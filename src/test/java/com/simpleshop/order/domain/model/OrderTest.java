package com.simpleshop.order.domain.model;

import com.simpleshop.order.domain.event.*;
import com.simpleshop.order.domain.exception.EmptyOrderException;
import com.simpleshop.order.domain.exception.InvalidOrderStateException;
import com.simpleshop.order.domain.model.vo.OrderStatus;
import com.simpleshop.shared.domain.model.vo.Address;
import com.simpleshop.shared.domain.model.vo.Money;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.*;

public class OrderTest {

    private UUID userId;
    private Address shippingAddress;
    private List<OrderItem> orderItems;
    private OrderItem item1;
    private OrderItem item2;

    @BeforeMethod
    public void setUp() {
        userId = UUID.randomUUID();
        shippingAddress = Address.of("123 Main St", "New York", "10001", "USA");
        
        item1 = OrderItem.create(
            UUID.randomUUID(),
            "Product A",
            2,
            Money.usd(new BigDecimal("25.00")),
            UUID.randomUUID()
        );
        
        item2 = OrderItem.create(
            UUID.randomUUID(),
            "Product B",
            1,
            Money.usd(new BigDecimal("50.00")),
            UUID.randomUUID()
        );
        
        orderItems = List.of(item1, item2);
    }

    @Test
    public void place_createsOrderWithCorrectState() {
        Order order = Order.place(userId, shippingAddress, orderItems);

        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals(order.getUserId(), userId);
        assertEquals(order.getShippingAddress(), shippingAddress);
        assertEquals(order.getItems().size(), 2);
        assertEquals(order.getStatus(), OrderStatus.PENDING);
        assertNotNull(order.getCreatedAt());
        assertNull(order.getPaidAt());
    }

    @Test
    public void place_calculatesTotalCorrectly() {
        Order order = Order.place(userId, shippingAddress, orderItems);

        Money expectedTotal = Money.usd(new BigDecimal("100.00"));
        assertEquals(order.getTotalAmount(), expectedTotal);
    }

    @Test
    public void place_registersOrderPlacedEvent() {
        Order order = Order.place(userId, shippingAddress, orderItems);

        var events = order.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof OrderPlaced);
        
        OrderPlaced event = (OrderPlaced) events.iterator().next();
        assertEquals(event.getOrderId(), order.getId());
        assertEquals(event.getOrderNumber(), order.getOrderNumber().getValue());
        assertEquals(event.getUserId(), userId);
        assertEquals(event.getItemCount(), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void place_throwsForNullUserId() {
        Order.place(null, shippingAddress, orderItems);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void place_throwsForNullAddress() {
        Order.place(userId, null, orderItems);
    }

    @Test(expectedExceptions = EmptyOrderException.class)
    public void place_throwsForEmptyItems() {
        Order.place(userId, shippingAddress, List.of());
    }

    @Test(expectedExceptions = EmptyOrderException.class)
    public void place_throwsForNullItems() {
        Order.place(userId, shippingAddress, null);
    }

    @Test
    public void confirm_changesStatusToConfirmed() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.clearEvents();

        order.confirm();

        assertEquals(order.getStatus(), OrderStatus.CONFIRMED);
        assertNotNull(order.getPaidAt());
    }

    @Test
    public void confirm_registersOrderConfirmedEvent() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.clearEvents();

        order.confirm();

        var events = order.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof OrderConfirmed);
        
        OrderConfirmed event = (OrderConfirmed) events.iterator().next();
        assertEquals(event.getOrderId(), order.getId());
        assertEquals(event.getOrderNumber(), order.getOrderNumber().getValue());
    }

    @Test
    public void startProcessing_changesStatusToProcessing() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();

        order.startProcessing();

        assertEquals(order.getStatus(), OrderStatus.PROCESSING);
    }

    @Test(expectedExceptions = InvalidOrderStateException.class)
    public void startProcessing_throwsFromPendingStatus() {
        Order order = Order.place(userId, shippingAddress, orderItems);

        order.startProcessing();
    }

    @Test
    public void ship_changesStatusToShipped() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();
        order.clearEvents();

        order.ship();

        assertEquals(order.getStatus(), OrderStatus.SHIPPED);
        assertNotNull(order.getShippedAt());
    }

    @Test
    public void ship_registersOrderShippedEvent() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();
        order.clearEvents();

        order.ship();

        var events = order.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof OrderShipped);
    }

    @Test(expectedExceptions = InvalidOrderStateException.class)
    public void ship_throwsFromPendingStatus() {
        Order order = Order.place(userId, shippingAddress, orderItems);

        order.ship();
    }

    @Test
    public void deliver_changesStatusToDelivered() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();
        order.ship();
        order.clearEvents();

        order.deliver();

        assertEquals(order.getStatus(), OrderStatus.DELIVERED);
        assertNotNull(order.getDeliveredAt());
    }

    @Test
    public void deliver_registersOrderDeliveredEvent() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();
        order.ship();
        order.clearEvents();

        order.deliver();

        var events = order.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof OrderDelivered);
    }

    @Test(expectedExceptions = InvalidOrderStateException.class)
    public void deliver_throwsFromProcessingStatus() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();

        order.deliver();
    }

    @Test
    public void cancel_changesStatusToCancelled() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.clearEvents();

        order.cancel("Customer request");

        assertEquals(order.getStatus(), OrderStatus.CANCELLED);
        assertNotNull(order.getCancelledAt());
        assertEquals(order.getCancellationReason(), "Customer request");
    }

    @Test
    public void cancel_registersOrderCancelledEvent() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.clearEvents();

        order.cancel("Out of stock");

        var events = order.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof OrderCancelled);
        
        OrderCancelled event = (OrderCancelled) events.iterator().next();
        assertEquals(event.getOrderId(), order.getId());
        assertEquals(event.getReason(), "Out of stock");
    }

    @Test
    public void cancel_canCancelFromConfirmedStatus() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();

        order.cancel("Changed mind");

        assertEquals(order.getStatus(), OrderStatus.CANCELLED);
    }

    @Test
    public void cancel_canCancelFromProcessingStatus() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();

        order.cancel("Issue found");

        assertEquals(order.getStatus(), OrderStatus.CANCELLED);
    }

    @Test(expectedExceptions = InvalidOrderStateException.class)
    public void cancel_throwsFromShippedStatus() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();
        order.ship();

        order.cancel("Too late");
    }

    @Test(expectedExceptions = InvalidOrderStateException.class)
    public void cancel_throwsFromDeliveredStatus() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        order.cancel("Too late");
    }

    @Test(expectedExceptions = InvalidOrderStateException.class)
    public void confirm_throwsFromCancelledStatus() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.cancel("Cancelled");

        order.confirm();
    }

    @Test
    public void getItemCount_returnsTotalQuantity() {
        Order order = Order.place(userId, shippingAddress, orderItems);

        assertEquals(order.getItemCount(), 3);
    }

    @Test
    public void isPending_returnsTrueForPendingOrder() {
        Order order = Order.place(userId, shippingAddress, orderItems);

        assertTrue(order.isPending());
        assertFalse(order.isConfirmed());
        assertFalse(order.isCancelled());
        assertFalse(order.isDelivered());
    }

    @Test
    public void isConfirmed_returnsTrueForConfirmedOrder() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();

        assertFalse(order.isPending());
        assertTrue(order.isConfirmed());
    }

    @Test
    public void isCancelled_returnsTrueForCancelledOrder() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.cancel("Test");

        assertTrue(order.isCancelled());
    }

    @Test
    public void isDelivered_returnsTrueForDeliveredOrder() {
        Order order = Order.place(userId, shippingAddress, orderItems);
        order.confirm();
        order.startProcessing();
        order.ship();
        order.deliver();

        assertTrue(order.isDelivered());
    }

    @Test
    public void getItems_returnsUnmodifiableList() {
        Order order = Order.place(userId, shippingAddress, orderItems);

        assertThrows(UnsupportedOperationException.class, () -> {
            order.getItems().add(item1);
        });
    }
}
