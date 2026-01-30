package com.simpleshop.order.domain.model;

import com.simpleshop.order.domain.event.*;
import com.simpleshop.order.domain.exception.EmptyOrderException;
import com.simpleshop.order.domain.exception.InvalidOrderStateException;
import com.simpleshop.order.domain.model.vo.OrderId;
import com.simpleshop.order.domain.model.vo.OrderNumber;
import com.simpleshop.order.domain.model.vo.OrderStatus;
import com.simpleshop.shared.domain.model.AggregateRoot;
import com.simpleshop.shared.domain.model.vo.Address;
import com.simpleshop.shared.domain.model.vo.Money;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "orders")
public class Order extends AggregateRoot<Order> {
    
    @Id
    private UUID id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "order_number", nullable = false, unique = true))
    private OrderNumber orderNumber;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItem> items = new ArrayList<>();
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "shipping_street", nullable = false)),
        @AttributeOverride(name = "city", column = @Column(name = "shipping_city", nullable = false)),
        @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code", nullable = false)),
        @AttributeOverride(name = "country", column = @Column(name = "shipping_country", nullable = false))
    })
    private Address shippingAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false)),
        @AttributeOverride(name = "currencyCode", column = @Column(name = "total_currency", nullable = false, length = 3))
    })
    private Money totalAmount;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column
    private Instant paidAt;
    
    @Column
    private Instant shippedAt;
    
    @Column
    private Instant deliveredAt;
    
    @Column
    private Instant cancelledAt;
    
    @Column
    private String cancellationReason;
    
    protected Order() {}
    
    private Order(OrderNumber orderNumber, UUID userId, Address shippingAddress, List<OrderItem> items, Money totalAmount) {
        this.id = UUID.randomUUID();
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.items = new ArrayList<>(items);
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
    }
    
    public static Order place(OrderNumber orderNumber, UUID userId, Address shippingAddress, List<OrderItem> items) {
        if (orderNumber == null) throw new IllegalArgumentException("Order number cannot be null");
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        if (shippingAddress == null) throw new IllegalArgumentException("Shipping address cannot be null");
        if (items == null || items.isEmpty()) throw new EmptyOrderException();
        
        Money total = calculateTotal(items);
        Order order = new Order(orderNumber, userId, shippingAddress, items, total);
        
        order.registerEvent(new OrderPlaced(
            order.id,
            order.orderNumber.getValue(),
            order.userId,
            order.totalAmount.getAmount(),
            order.totalAmount.getCurrencyCode(),
            order.items.size()
        ));
        
        return order;
    }
    
    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.usd(BigDecimal.ZERO), Money::add);
    }
    
    public void confirm() {
        validateTransition(OrderStatus.CONFIRMED);
        this.status = OrderStatus.CONFIRMED;
        this.paidAt = Instant.now();
        registerEvent(new OrderConfirmed(id, orderNumber.getValue(), userId));
    }
    
    public void startProcessing() {
        validateTransition(OrderStatus.PROCESSING);
        this.status = OrderStatus.PROCESSING;
    }
    
    public void ship() {
        validateTransition(OrderStatus.SHIPPED);
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = Instant.now();
        registerEvent(new OrderShipped(id, orderNumber.getValue(), userId));
    }
    
    public void deliver() {
        validateTransition(OrderStatus.DELIVERED);
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = Instant.now();
        registerEvent(new OrderDelivered(id, orderNumber.getValue(), userId));
    }
    
    public void cancel(String reason) {
        validateTransition(OrderStatus.CANCELLED);
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        this.cancellationReason = reason;
        registerEvent(new OrderCancelled(id, orderNumber.getValue(), userId, reason));
    }
    
    private void validateTransition(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(status, newStatus);
        }
    }
    
    public UUID getId() {
        return id;
    }
    
    public OrderId getOrderId() {
        return OrderId.of(id);
    }
    
    public OrderNumber getOrderNumber() {
        return orderNumber;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public Address getShippingAddress() {
        return shippingAddress;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public Money getTotalAmount() {
        return totalAmount;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getPaidAt() {
        return paidAt;
    }
    
    public Instant getShippedAt() {
        return shippedAt;
    }
    
    public Instant getDeliveredAt() {
        return deliveredAt;
    }
    
    public Instant getCancelledAt() {
        return cancelledAt;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public int getItemCount() {
        return items.stream()
            .mapToInt(item -> item.getQuantity().getValue())
            .sum();
    }
    
    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }
    
    public boolean isConfirmed() {
        return status == OrderStatus.CONFIRMED;
    }
    
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }
    
    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }
}
