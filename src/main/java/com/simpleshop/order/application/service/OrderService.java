package com.simpleshop.order.application.service;

import com.simpleshop.cart.application.port.in.ClearCartUseCase;
import com.simpleshop.cart.application.port.in.GetCartUseCase;
import com.simpleshop.cart.application.command.ClearCartCommand;
import com.simpleshop.cart.application.query.CartItemView;
import com.simpleshop.cart.application.query.CartView;
import com.simpleshop.cart.application.query.GetCartQuery;
import com.simpleshop.catalog.application.port.out.ProductRepository;
import com.simpleshop.catalog.domain.model.Product;
import com.simpleshop.catalog.domain.model.vo.ProductId;
import com.simpleshop.inventory.application.command.AllocateStockCommand;
import com.simpleshop.inventory.application.port.in.AllocateStockUseCase;
import com.simpleshop.inventory.application.query.StockAllocationResult;
import com.simpleshop.order.application.command.*;
import com.simpleshop.order.application.port.in.*;
import com.simpleshop.order.application.port.out.OrderNumberGenerator;
import com.simpleshop.order.application.port.out.OrderRepository;
import com.simpleshop.order.application.query.*;
import com.simpleshop.order.domain.exception.EmptyOrderException;
import com.simpleshop.order.domain.exception.OrderNotFoundException;
import com.simpleshop.order.domain.model.Order;
import com.simpleshop.order.domain.model.OrderItem;
import com.simpleshop.order.domain.model.vo.OrderId;
import com.simpleshop.order.domain.model.vo.OrderNumber;
import com.simpleshop.shared.domain.model.vo.Address;
import com.simpleshop.shared.domain.model.vo.Money;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderService implements PlaceOrderUseCase, PlaceOrderFromCartUseCase, ConfirmOrderUseCase,
        CancelOrderUseCase, ShipOrderUseCase, DeliverOrderUseCase, GetOrderUseCase, ListUserOrdersUseCase {
    
    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final ProductRepository productRepository;
    private final GetCartUseCase getCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final AllocateStockUseCase allocateStockUseCase;
    
    public OrderService(OrderRepository orderRepository, OrderNumberGenerator orderNumberGenerator,
                        ProductRepository productRepository, GetCartUseCase getCartUseCase, 
                        ClearCartUseCase clearCartUseCase, AllocateStockUseCase allocateStockUseCase) {
        this.orderRepository = orderRepository;
        this.orderNumberGenerator = orderNumberGenerator;
        this.productRepository = productRepository;
        this.getCartUseCase = getCartUseCase;
        this.clearCartUseCase = clearCartUseCase;
        this.allocateStockUseCase = allocateStockUseCase;
    }
    
    @Override
    @WithSpan("order.placeOrder")
    public OrderView execute(PlaceOrderCommand command) {
        List<AllocateStockCommand.AllocationRequest> allocationRequests = command.items().stream()
            .map(item -> new AllocateStockCommand.AllocationRequest(item.productId(), item.quantity()))
            .toList();
        
        StockAllocationResult allocationResult = allocateStockUseCase.allocate(
            new AllocateStockCommand(allocationRequests)
        );
        
        Address shippingAddress = Address.of(
            command.street(),
            command.city(),
            command.postalCode(),
            command.country()
        );
        
        List<OrderItem> orderItems = new ArrayList<>();
        for (PlaceOrderCommand.OrderItemData itemData : command.items()) {
            Product product = productRepository.findById(ProductId.of(itemData.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemData.productId()));
            
            com.simpleshop.catalog.domain.model.vo.Money catalogPrice = product.getPrice();
            Money sharedPrice = Money.of(catalogPrice.getAmount(), catalogPrice.getCurrency());
            UUID warehouseId = allocationResult.getWarehouseIdForProduct(itemData.productId());
            
            OrderItem item = OrderItem.create(
                product.getId(),
                product.getName(),
                itemData.quantity(),
                sharedPrice,
                warehouseId
            );
            orderItems.add(item);
        }
        
        OrderNumber orderNumber = orderNumberGenerator.generate();
        Order order = Order.place(orderNumber, command.userId(), shippingAddress, orderItems);
        order = orderRepository.save(order);
        
        return toOrderView(order);
    }
    
    @Override
    @WithSpan("order.placeOrderFromCart")
    public OrderView execute(PlaceOrderFromCartCommand command) {
        CartView cart = getCartUseCase.execute(new GetCartQuery(command.sessionId(), command.userId()));
        
        if (cart.items().isEmpty()) {
            throw new EmptyOrderException();
        }
        
        List<AllocateStockCommand.AllocationRequest> allocationRequests = cart.items().stream()
            .map(item -> new AllocateStockCommand.AllocationRequest(item.productId(), item.quantity()))
            .toList();
        
        StockAllocationResult allocationResult = allocateStockUseCase.allocate(
            new AllocateStockCommand(allocationRequests)
        );
        
        Address shippingAddress = Address.of(
            command.street(),
            command.city(),
            command.postalCode(),
            command.country()
        );
        
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemView cartItem : cart.items()) {
            UUID warehouseId = allocationResult.getWarehouseIdForProduct(cartItem.productId());
            OrderItem item = OrderItem.create(
                cartItem.productId(),
                cartItem.productName(),
                cartItem.quantity(),
                Money.of(cartItem.price(), cartItem.currency()),
                warehouseId
            );
            orderItems.add(item);
        }
        
        UUID userId = command.userId() != null ? command.userId() : 
            UUID.fromString("00000000-0000-0000-0000-000000000000");
        
        OrderNumber orderNumber = orderNumberGenerator.generate();
        Order order = Order.place(orderNumber, userId, shippingAddress, orderItems);
        order = orderRepository.save(order);
        
        clearCartUseCase.execute(new ClearCartCommand(command.sessionId(), command.userId()));
        
        return toOrderView(order);
    }
    
    @Override
    @WithSpan("order.confirmOrder")
    public OrderView execute(ConfirmOrderCommand command) {
        Order order = findOrderById(command.orderId());
        order.confirm();
        order = orderRepository.save(order);
        return toOrderView(order);
    }
    
    @Override
    @WithSpan("order.cancelOrder")
    public void execute(CancelOrderCommand command) {
        Order order = findOrderById(command.orderId());
        order.cancel(command.reason());
        orderRepository.save(order);
    }
    
    @Override
    @WithSpan("order.shipOrder")
    public OrderView execute(ShipOrderCommand command) {
        Order order = findOrderById(command.orderId());
        order.ship();
        order = orderRepository.save(order);
        return toOrderView(order);
    }
    
    @Override
    @WithSpan("order.deliverOrder")
    public OrderView execute(DeliverOrderCommand command) {
        Order order = findOrderById(command.orderId());
        order.deliver();
        order = orderRepository.save(order);
        return toOrderView(order);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("order.getOrder")
    public Optional<OrderView> execute(GetOrderQuery query) {
        return orderRepository.findById(OrderId.of(query.orderId()))
            .map(this::toOrderView);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("order.getOrderByNumber")
    public Optional<OrderView> execute(GetOrderByNumberQuery query) {
        return orderRepository.findByOrderNumber(OrderNumber.of(query.orderNumber()))
            .map(this::toOrderView);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("order.listUserOrders")
    public Page<OrderSummaryView> execute(ListUserOrdersQuery query) {
        PageRequest pageable = PageRequest.of(query.page(), query.size());
        return orderRepository.findByUserId(query.userId(), pageable)
            .map(this::toOrderSummaryView);
    }
    
    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(OrderId.of(orderId))
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
    
    private OrderView toOrderView(Order order) {
        List<OrderItemView> itemViews = order.getItems().stream()
            .map(this::toOrderItemView)
            .toList();
        
        return new OrderView(
            order.getId(),
            order.getOrderNumber().getValue(),
            order.getUserId(),
            itemViews,
            order.getShippingAddress().getStreet(),
            order.getShippingAddress().getCity(),
            order.getShippingAddress().getPostalCode(),
            order.getShippingAddress().getCountry(),
            order.getStatus().name(),
            order.getTotalAmount().getAmount(),
            order.getTotalAmount().getCurrencyCode(),
            order.getItemCount(),
            order.getCreatedAt(),
            order.getPaidAt(),
            order.getShippedAt(),
            order.getDeliveredAt(),
            order.getCancelledAt(),
            order.getCancellationReason()
        );
    }
    
    private OrderItemView toOrderItemView(OrderItem item) {
        return new OrderItemView(
            item.getId(),
            item.getProductId(),
            item.getProductName(),
            item.getQuantity().getValue(),
            item.getUnitPrice().getAmount(),
            item.getUnitPrice().getCurrencyCode(),
            item.getSubtotal().getAmount(),
            item.getWarehouseId()
        );
    }
    
    private OrderSummaryView toOrderSummaryView(Order order) {
        return new OrderSummaryView(
            order.getId(),
            order.getOrderNumber().getValue(),
            order.getStatus().name(),
            order.getTotalAmount().getAmount(),
            order.getTotalAmount().getCurrencyCode(),
            order.getItemCount(),
            order.getCreatedAt()
        );
    }
}
