package com.simpleshop.integration.persistence;

import com.simpleshop.SimpleShopApplication;
import com.simpleshop.cart.domain.model.Cart;
import com.simpleshop.cart.domain.model.vo.SessionId;
import com.simpleshop.cart.infrastructure.adapter.out.persistence.CartJpaRepository;
import com.simpleshop.catalog.domain.model.Product;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.catalog.domain.model.vo.Sku;
import com.simpleshop.catalog.infrastructure.adapter.out.persistence.ProductJpaRepository;
import com.simpleshop.inventory.domain.model.Stock;
import com.simpleshop.inventory.domain.model.Warehouse;
import com.simpleshop.inventory.infrastructure.adapter.out.persistence.JpaStockRepository;
import com.simpleshop.inventory.infrastructure.adapter.out.persistence.JpaWarehouseRepository;
import com.simpleshop.order.domain.model.Order;
import com.simpleshop.order.domain.model.OrderItem;
import com.simpleshop.order.domain.model.vo.OrderNumber;
import com.simpleshop.order.infrastructure.adapter.out.persistence.OrderJpaRepository;
import com.simpleshop.shared.domain.model.vo.Address;
import com.simpleshop.shared.domain.model.vo.Quantity;
import com.simpleshop.shipping.domain.model.Shipment;
import com.simpleshop.shipping.domain.model.vo.TrackingNumber;
import com.simpleshop.shipping.infrastructure.adapter.out.persistence.ShipmentJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SimpleShopApplication.class)
@ActiveProfiles("test")
@Transactional
class PersistenceRepositoryIT {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private CartJpaRepository cartJpaRepository;

    @Autowired
    private JpaStockRepository stockRepository;

    @Autowired
    private JpaWarehouseRepository warehouseRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private ShipmentJpaRepository shipmentJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        shipmentJpaRepository.deleteAll();
        orderJpaRepository.deleteAll();
        cartJpaRepository.deleteAll();
        stockRepository.deleteAll();
        warehouseRepository.deleteAll();
        productJpaRepository.deleteAll();
    }

    @Test
    void intP001_shouldFilterProductsByCategoryAndActiveFlag() {
        UUID categoryA = UUID.randomUUID();
        UUID categoryB = UUID.randomUUID();

        Product activeA1 = productJpaRepository.save(Product.create(
            "Alpha", "", Sku.of("sku-alpha"), Money.usd(new BigDecimal("10.00")), categoryA, null
        ));
        Product activeA2 = productJpaRepository.save(Product.create(
            "Beta", "", Sku.of("sku-beta"), Money.usd(new BigDecimal("20.00")), categoryA, null
        ));
        Product activeB = productJpaRepository.save(Product.create(
            "Gamma", "", Sku.of("sku-gamma"), Money.usd(new BigDecimal("30.00")), categoryB, null
        ));
        Product inactiveA = Product.create(
            "Inactive", "", Sku.of("sku-inactive"), Money.usd(new BigDecimal("40.00")), categoryA, null
        );
        inactiveA.deactivate();
        productJpaRepository.save(inactiveA);

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        Page<Product> activeOnlyAllCategories = productJpaRepository.findAllWithFilters(null, true, pageable);
        assertEquals(3, activeOnlyAllCategories.getTotalElements());
        assertTrue(activeOnlyAllCategories.stream().allMatch(Product::isActive));

        Page<Product> activeOnlyCategoryA = productJpaRepository.findAllWithFilters(categoryA, true, pageable);
        assertEquals(2, activeOnlyCategoryA.getTotalElements());
        assertTrue(activeOnlyCategoryA.getContent().stream().allMatch(p -> categoryA.equals(p.getCategoryId())));
        assertTrue(activeOnlyCategoryA.stream().allMatch(Product::isActive));

        Page<Product> allCategoryA = productJpaRepository.findAllWithFilters(categoryA, false, pageable);
        assertEquals(3, allCategoryA.getTotalElements());

        Page<Product> stableOrderCheck = productJpaRepository.findAllWithFilters(categoryA, true, pageable);
        assertEquals(
            activeOnlyCategoryA.getContent().stream().map(Product::getId).toList(),
            stableOrderCheck.getContent().stream().map(Product::getId).toList()
        );

        assertTrue(activeOnlyCategoryA.getTotalPages() >= 1);
        assertEquals(10, activeOnlyCategoryA.getSize());

        assertNotNull(activeA1.getId());
        assertNotNull(activeA2.getId());
        assertNotNull(activeB.getId());
    }

    @Test
    void intP002_shouldFetchCartWithItemsBySessionUserAndId() {
        UUID userId = UUID.randomUUID();
        UUID product1 = UUID.randomUUID();
        UUID product2 = UUID.randomUUID();

        Cart sessionCart = Cart.createForSession(SessionId.of("session-abc"));
        sessionCart.addItem(product1, Money.usd(new BigDecimal("10.00")), 1);
        sessionCart.addItem(product2, Money.usd(new BigDecimal("12.00")), 2);
        sessionCart = cartJpaRepository.save(sessionCart);

        Cart userCart = Cart.createForUser(userId);
        userCart.addItem(product1, Money.usd(new BigDecimal("10.00")), 3);
        userCart.addItem(product2, Money.usd(new BigDecimal("12.00")), 4);
        userCart = cartJpaRepository.save(userCart);

        entityManager.flush();
        entityManager.clear();

        Cart loadedBySession = cartJpaRepository.findBySessionIdValue("session-abc").orElseThrow();
        assertEquals(sessionCart.getId(), loadedBySession.getId());
        assertEquals(2, loadedBySession.getItems().size());

        Cart loadedByUser = cartJpaRepository.findByUserId(userId).orElseThrow();
        assertEquals(userCart.getId(), loadedByUser.getId());
        assertEquals(2, loadedByUser.getItems().size());

        Cart loadedById = cartJpaRepository.findByIdWithItems(userCart.getId()).orElseThrow();
        assertEquals(2, loadedById.getItems().size());

        entityManager.detach(loadedById);
        assertDoesNotThrow(() -> loadedById.getItems().size());
    }

    @Test
    void intP003_shouldAggregateStockAvailabilityAndReservedQuantities() {
        UUID productId = UUID.randomUUID();
        UUID missingProductId = UUID.randomUUID();

        Warehouse warehouse1 = warehouseRepository.save(Warehouse.create(
            "Warehouse-A", Address.of("Street 1", "New York", "10001", "USA")
        ));
        Warehouse warehouse2 = warehouseRepository.save(Warehouse.create(
            "Warehouse-B", Address.of("Street 2", "Austin", "73301", "USA")
        ));

        Stock stock1 = Stock.create(productId, warehouse1.getId(), Quantity.of(10));
        stock1.reserve(Quantity.of(3));
        stockRepository.save(stock1);

        Stock stock2 = Stock.create(productId, warehouse2.getId(), Quantity.of(5));
        stock2.reserve(Quantity.of(1));
        stockRepository.save(stock2);

        int available = stockRepository.sumAvailableByProductId(productId);
        int reserved = stockRepository.sumReservedByProductId(productId);

        assertEquals(11, available);
        assertEquals(4, reserved);

        assertEquals(0, stockRepository.sumAvailableByProductId(missingProductId));
        assertEquals(0, stockRepository.sumReservedByProductId(missingProductId));
    }

    @Test
    void intP004_shouldLookupOrdersAndSortByCreatedAtDesc() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        Order first = orderJpaRepository.save(newOrder("ORD-2026-00001", userA, "Item 1", 1));
        pauseForOrdering();
        Order second = orderJpaRepository.save(newOrder("ORD-2026-00002", userA, "Item 2", 2));
        pauseForOrdering();
        Order third = orderJpaRepository.save(newOrder("ORD-2026-00003", userB, "Item 3", 1));

        Order foundByNumber = orderJpaRepository.findByOrderNumberValue("ORD-2026-00002").orElseThrow();
        assertEquals(second.getId(), foundByNumber.getId());

        Page<Order> userAOrders = orderJpaRepository.findByUserIdOrderByCreatedAtDesc(userA, PageRequest.of(0, 10));
        assertEquals(List.of(second.getId(), first.getId()), userAOrders.map(Order::getId).toList());

        Page<Order> allOrders = orderJpaRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
        assertEquals(List.of(third.getId(), second.getId(), first.getId()), allOrders.map(Order::getId).toList());
    }

    @Test
    void intP005_shouldLookupShipmentsAndSortByCreatedAtDescForOrder() {
        UUID orderId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();

        Shipment first = shipmentJpaRepository.save(Shipment.create(
            TrackingNumber.of("SHIP-2026-00001"),
            orderId,
            warehouseId,
            Address.of("Street A", "Seattle", "98101", "USA"),
            LocalDate.now().plusDays(4)
        ));
        pauseForOrdering();
        Shipment second = shipmentJpaRepository.save(Shipment.create(
            TrackingNumber.of("SHIP-2026-00002"),
            orderId,
            warehouseId,
            Address.of("Street B", "Seattle", "98101", "USA"),
            LocalDate.now().plusDays(5)
        ));

        Shipment byTracking = shipmentJpaRepository.findByTrackingNumberValue("SHIP-2026-00001").orElseThrow();
        assertEquals(first.getId(), byTracking.getId());

        Page<Shipment> byOrder = shipmentJpaRepository.findByOrderIdOrderByCreatedAtDesc(orderId, PageRequest.of(0, 10));
        assertEquals(List.of(second.getId(), first.getId()), byOrder.map(Shipment::getId).toList());
    }

    private Order newOrder(String orderNumber, UUID userId, String itemName, int quantity) {
        OrderItem item = OrderItem.create(
            UUID.randomUUID(),
            itemName,
            quantity,
            com.simpleshop.shared.domain.model.vo.Money.usd(new BigDecimal("15.00")),
            UUID.randomUUID()
        );

        return Order.place(
            OrderNumber.of(orderNumber),
            userId,
            Address.of("123 Main", "Boston", "02110", "USA"),
            List.of(item)
        );
    }

    private void pauseForOrdering() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Interrupted while waiting to create deterministic ordering");
        }
    }
}
