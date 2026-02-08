package com.simpleshop.integration.orchestration;

import com.simpleshop.SimpleShopApplication;
import com.simpleshop.cart.domain.model.Cart;
import com.simpleshop.cart.domain.model.vo.SessionId;
import com.simpleshop.cart.infrastructure.adapter.out.persistence.CartJpaRepository;
import com.simpleshop.catalog.domain.model.Product;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.catalog.domain.model.vo.Sku;
import com.simpleshop.catalog.infrastructure.adapter.out.persistence.ProductJpaRepository;
import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.PersonName;
import com.simpleshop.identity.infrastructure.adapter.out.persistence.JpaUserRepository;
import com.simpleshop.inventory.domain.exception.InsufficientStockException;
import com.simpleshop.inventory.domain.model.Stock;
import com.simpleshop.inventory.domain.model.Warehouse;
import com.simpleshop.inventory.infrastructure.adapter.out.persistence.JpaStockRepository;
import com.simpleshop.inventory.infrastructure.adapter.out.persistence.JpaWarehouseRepository;
import com.simpleshop.notification.domain.model.NotificationLog;
import com.simpleshop.notification.domain.model.vo.NotificationStatus;
import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.notification.infrastructure.adapter.out.persistence.JpaNotificationLogRepository;
import com.simpleshop.order.application.command.CancelOrderCommand;
import com.simpleshop.order.application.command.ConfirmOrderCommand;
import com.simpleshop.order.application.command.PlaceOrderFromCartCommand;
import com.simpleshop.order.application.port.in.CancelOrderUseCase;
import com.simpleshop.order.application.port.in.ConfirmOrderUseCase;
import com.simpleshop.order.application.port.in.PlaceOrderFromCartUseCase;
import com.simpleshop.order.application.query.OrderView;
import com.simpleshop.order.domain.model.Order;
import com.simpleshop.order.domain.model.vo.OrderStatus;
import com.simpleshop.order.infrastructure.adapter.out.persistence.OrderJpaRepository;
import com.simpleshop.shared.domain.model.vo.Address;
import com.simpleshop.shared.domain.model.vo.Email;
import com.simpleshop.shared.domain.model.vo.Quantity;
import com.simpleshop.shipping.application.command.UpdateShipmentStatusCommand;
import com.simpleshop.shipping.application.port.in.UpdateShipmentStatusUseCase;
import com.simpleshop.shipping.domain.model.Shipment;
import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import com.simpleshop.shipping.infrastructure.adapter.out.persistence.ShipmentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SimpleShopApplication.class)
@ActiveProfiles("test")
class OrderOrchestrationIT {

    @Autowired
    private PlaceOrderFromCartUseCase placeOrderFromCartUseCase;

    @Autowired
    private ConfirmOrderUseCase confirmOrderUseCase;

    @Autowired
    private CancelOrderUseCase cancelOrderUseCase;

    @Autowired
    private UpdateShipmentStatusUseCase updateShipmentStatusUseCase;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private JpaWarehouseRepository warehouseRepository;

    @Autowired
    private JpaStockRepository stockRepository;

    @Autowired
    private CartJpaRepository cartRepository;

    @Autowired
    private OrderJpaRepository orderRepository;

    @Autowired
    private ShipmentJpaRepository shipmentRepository;

    @Autowired
    private JpaNotificationLogRepository notificationLogRepository;

    @Autowired
    private JpaUserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        notificationLogRepository.deleteAll();
        shipmentRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        stockRepository.deleteAll();
        warehouseRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void intP007_shouldPlaceOrderFromCartAndCommitAllChanges() {
        User user = createUser("buyer-1@example.com", false);
        Product product = createProduct("Phone", "sku-phone", "399.00");
        Warehouse warehouse = createWarehouse("WH-East");
        createStock(product.getId(), warehouse.getId(), 10);

        Cart cart = Cart.createForUser(user.getUserId().getValue());
        cart.addItem(product.getId(), product.getPrice(), 2);
        cartRepository.save(cart);

        OrderView order = placeOrderFromCartUseCase.execute(new PlaceOrderFromCartCommand(
            user.getUserId().getValue(),
            null,
            "123 River St",
            "Austin",
            "73301",
            "USA"
        ));

        Order persistedOrder = orderRepository.findById(order.id()).orElseThrow();
        assertEquals(OrderStatus.PENDING, persistedOrder.getStatus());

        Stock persistedStock = stockRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).orElseThrow();
        assertEquals(10, persistedStock.getQuantity().getValue());
        assertEquals(2, persistedStock.getReservedQuantity().getValue());

        Cart persistedCart = cartRepository.findByUserId(user.getUserId().getValue()).orElseThrow();
        assertEquals(0, persistedCart.getItems().size());
    }

    @Test
    void intP008_shouldRollbackOrderPlacementWhenStockIsInsufficient() {
        User user = createUser("buyer-2@example.com", false);

        Product productEnough = createProduct("Keyboard", "sku-keyboard", "99.00");
        Product productInsufficient = createProduct("Monitor", "sku-monitor", "199.00");

        Warehouse warehouse = createWarehouse("WH-North");
        createStock(productEnough.getId(), warehouse.getId(), 10);
        createStock(productInsufficient.getId(), warehouse.getId(), 1);

        Cart cart = Cart.createForUser(user.getUserId().getValue());
        cart.addItem(productEnough.getId(), productEnough.getPrice(), 2);
        cart.addItem(productInsufficient.getId(), productInsufficient.getPrice(), 2);
        cartRepository.save(cart);

        long ordersBefore = orderRepository.count();

        assertThrows(InsufficientStockException.class, () -> placeOrderFromCartUseCase.execute(new PlaceOrderFromCartCommand(
            user.getUserId().getValue(),
            null,
            "44 Pine Rd",
            "Denver",
            "80202",
            "USA"
        )));

        assertEquals(ordersBefore, orderRepository.count());

        Stock enoughStock = stockRepository.findByProductIdAndWarehouseId(productEnough.getId(), warehouse.getId()).orElseThrow();
        Stock insufficientStock = stockRepository.findByProductIdAndWarehouseId(productInsufficient.getId(), warehouse.getId()).orElseThrow();
        assertEquals(0, enoughStock.getReservedQuantity().getValue());
        assertEquals(0, insufficientStock.getReservedQuantity().getValue());

        Cart persistedCart = cartRepository.findByUserId(user.getUserId().getValue()).orElseThrow();
        assertEquals(2, persistedCart.getItems().size());
    }

    @Test
    void intP101_shouldConfirmOrderAndTriggerStockConfirmationAndShipmentCreation() {
        User user = createUser("buyer-3@example.com", false);
        Product product = createProduct("Laptop", "sku-laptop", "1099.00");
        Warehouse warehouse = createWarehouse("WH-West");
        createStock(product.getId(), warehouse.getId(), 10);

        Cart cart = Cart.createForUser(user.getUserId().getValue());
        cart.addItem(product.getId(), product.getPrice(), 2);
        cartRepository.save(cart);

        OrderView placed = placeOrderFromCartUseCase.execute(new PlaceOrderFromCartCommand(
            user.getUserId().getValue(),
            null,
            "500 Market St",
            "San Francisco",
            "94105",
            "USA"
        ));

        confirmOrderUseCase.execute(new ConfirmOrderCommand(placed.id()));

        waitForCondition(Duration.ofSeconds(5), () -> {
            Order order = orderRepository.findById(placed.id()).orElseThrow();
            return order.getStatus() == OrderStatus.CONFIRMED;
        }, "Order did not transition to CONFIRMED");

        waitForCondition(Duration.ofSeconds(5), () -> {
            Stock stock = stockRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).orElseThrow();
            return stock.getQuantity().getValue() == 8 && stock.getReservedQuantity().getValue() == 0;
        }, "Stock reservation was not confirmed");

        waitForCondition(Duration.ofSeconds(5), () ->
            shipmentRepository.findByOrderIdOrderByCreatedAtDesc(placed.id(), org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements() >= 1,
            "Shipment was not created after confirmation"
        );
    }

    @Test
    void intP102_shouldReleaseReservedStockWhenOrderIsCancelled() {
        User user = createUser("buyer-4@example.com", false);
        Product product = createProduct("Mouse", "sku-mouse", "49.00");
        Warehouse warehouse = createWarehouse("WH-South");
        createStock(product.getId(), warehouse.getId(), 7);

        Cart cart = Cart.createForUser(user.getUserId().getValue());
        cart.addItem(product.getId(), product.getPrice(), 3);
        cartRepository.save(cart);

        OrderView placed = placeOrderFromCartUseCase.execute(new PlaceOrderFromCartCommand(
            user.getUserId().getValue(),
            null,
            "1 Fleet St",
            "Miami",
            "33101",
            "USA"
        ));

        cancelOrderUseCase.execute(new CancelOrderCommand(placed.id(), "Customer changed mind"));

        waitForCondition(Duration.ofSeconds(5), () -> {
            Order order = orderRepository.findById(placed.id()).orElseThrow();
            return order.getStatus() == OrderStatus.CANCELLED;
        }, "Order did not transition to CANCELLED");

        waitForCondition(Duration.ofSeconds(5), () -> {
            Stock stock = stockRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId()).orElseThrow();
            return stock.getQuantity().getValue() == 7 && stock.getReservedQuantity().getValue() == 0;
        }, "Reserved stock was not released");
    }

    @Test
    void intP103_shouldCreateNotificationLogsWhenOrderIsConfirmed() {
        User user = createUser("buyer-5@example.com", false);
        Product product = createProduct("Tablet", "sku-tablet", "599.00");
        Warehouse warehouse = createWarehouse("WH-Central");
        createStock(product.getId(), warehouse.getId(), 5);

        Cart cart = Cart.createForUser(user.getUserId().getValue());
        cart.addItem(product.getId(), product.getPrice(), 1);
        cartRepository.save(cart);

        OrderView placed = placeOrderFromCartUseCase.execute(new PlaceOrderFromCartCommand(
            user.getUserId().getValue(),
            null,
            "90 Cedar Ave",
            "Chicago",
            "60601",
            "USA"
        ));

        confirmOrderUseCase.execute(new ConfirmOrderCommand(placed.id()));

        waitForCondition(Duration.ofSeconds(5), () -> {
            List<NotificationLog> logs = notificationLogRepository.findAll();
            boolean hasOrderConfirmation = logs.stream().anyMatch(log ->
                log.getType() == NotificationType.ORDER_CONFIRMATION &&
                    user.getEmail().getValue().equals(log.getRecipientEmail().getValue())
            );
            boolean hasInvoice = logs.stream().anyMatch(log ->
                log.getType() == NotificationType.INVOICE &&
                    user.getEmail().getValue().equals(log.getRecipientEmail().getValue())
            );
            return hasOrderConfirmation && hasInvoice;
        }, "Expected notification logs were not created");

        List<NotificationLog> relevantLogs = notificationLogRepository.findAll().stream()
            .filter(log -> log.getType() == NotificationType.ORDER_CONFIRMATION || log.getType() == NotificationType.INVOICE)
            .toList();

        assertFalse(relevantLogs.isEmpty());
        assertTrue(relevantLogs.stream().allMatch(log ->
            log.getStatus() == NotificationStatus.SENT || log.getStatus() == NotificationStatus.FAILED
        ));
    }

    @Test
    void intP104_shouldOnlyCreateShipmentStatusNotificationForImportantStatuses() {
        User user = createUser("buyer-6@example.com", false);
        Product product = createProduct("Camera", "sku-camera", "799.00");
        Warehouse warehouse = createWarehouse("WH-Coast");
        createStock(product.getId(), warehouse.getId(), 8);

        Cart cart = Cart.createForUser(user.getUserId().getValue());
        cart.addItem(product.getId(), product.getPrice(), 1);
        cartRepository.save(cart);

        OrderView placed = placeOrderFromCartUseCase.execute(new PlaceOrderFromCartCommand(
            user.getUserId().getValue(),
            null,
            "12 Bay Rd",
            "Portland",
            "97201",
            "USA"
        ));

        confirmOrderUseCase.execute(new ConfirmOrderCommand(placed.id()));

        Shipment shipment = waitForShipment(placed.id());

        long baselineUpdateLogCount = countLogsByType(NotificationType.SHIPMENT_UPDATE);

        updateShipmentStatusUseCase.updateStatus(new UpdateShipmentStatusCommand(
            shipment.getId(), ShipmentStatus.PICKED, "Warehouse floor", "Picked by operator"
        ));

        waitForSmallDelay();
        assertEquals(baselineUpdateLogCount, countLogsByType(NotificationType.SHIPMENT_UPDATE));

        updateShipmentStatusUseCase.updateStatus(new UpdateShipmentStatusCommand(
            shipment.getId(), ShipmentStatus.PACKED, "Packing zone", "Packed"
        ));

        waitForSmallDelay();
        assertEquals(baselineUpdateLogCount, countLogsByType(NotificationType.SHIPMENT_UPDATE));

        updateShipmentStatusUseCase.updateStatus(new UpdateShipmentStatusCommand(
            shipment.getId(), ShipmentStatus.SHIPPED, "Dock", "Handed to carrier"
        ));

        waitForCondition(Duration.ofSeconds(5), () ->
            countLogsByType(NotificationType.SHIPMENT_UPDATE) == baselineUpdateLogCount + 1,
            "Expected shipment update notification log was not created"
        );
    }

    private User createUser(String email, boolean admin) {
        User user = admin
            ? User.registerAdmin(Email.of(email), "$2a$10$wNb3ti6muvf95lM8UX4fbeDFq66M3M5fI4IN.87Qf2.9L7Q8V3f4W", PersonName.of("Admin", "User"))
            : User.register(Email.of(email), "$2a$10$wNb3ti6muvf95lM8UX4fbeDFq66M3M5fI4IN.87Qf2.9L7Q8V3f4W", PersonName.of("Test", "User"));
        return userRepository.save(user);
    }

    private Product createProduct(String name, String sku, String amount) {
        return productRepository.save(Product.create(
            name,
            name + " description",
            Sku.of(sku),
            Money.usd(new BigDecimal(amount)),
            UUID.randomUUID(),
            null
        ));
    }

    private Warehouse createWarehouse(String name) {
        return warehouseRepository.save(Warehouse.create(
            name,
            Address.of("10 Warehouse Ave", "Dallas", "75001", "USA")
        ));
    }

    private Stock createStock(UUID productId, UUID warehouseId, int quantity) {
        Stock stock = Stock.create(productId, warehouseId, Quantity.of(quantity));
        return stockRepository.save(stock);
    }

    private Shipment waitForShipment(UUID orderId) {
        waitForCondition(Duration.ofSeconds(5), () ->
            shipmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements() >= 1,
            "Shipment was not created"
        );

        return shipmentRepository.findByOrderIdOrderByCreatedAtDesc(orderId, org.springframework.data.domain.Pageable.unpaged())
            .getContent()
            .getFirst();
    }

    private long countLogsByType(NotificationType type) {
        return notificationLogRepository.findAll().stream()
            .filter(log -> log.getType() == type)
            .count();
    }

    private void waitForCondition(Duration timeout, BooleanSupplier condition, String errorMessage) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();

        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            waitForSmallDelay();
        }

        fail(errorMessage);
    }

    private void waitForSmallDelay() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting for async condition");
        }
    }
}
