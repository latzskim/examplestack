package com.simpleshop.order.domain.model.vo;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class OrderStatusTest {

    @Test
    public void canTransitionTo_pendingToConfirmed() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CONFIRMED));
    }

    @Test
    public void canTransitionTo_pendingToCancelled() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    public void canTransitionTo_pendingToProcessing_notAllowed() {
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.PROCESSING));
    }

    @Test
    public void canTransitionTo_confirmedToProcessing() {
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PROCESSING));
    }

    @Test
    public void canTransitionTo_confirmedToCancelled_notAllowed() {
        assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    public void canTransitionTo_confirmedToShipped() {
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.SHIPPED));
    }

    @Test
    public void canTransitionTo_processingToShipped() {
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.SHIPPED));
    }

    @Test
    public void canTransitionTo_processingToCancelled_notAllowed() {
        assertFalse(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    public void canTransitionTo_shippedToDelivered() {
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    public void canTransitionTo_shippedToCancelled_notAllowed() {
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    public void canTransitionTo_deliveredToAnything_notAllowed() {
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.SHIPPED));
    }

    @Test
    public void canTransitionTo_cancelledToAnything_notAllowed() {
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CONFIRMED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PENDING));
    }

    @Test
    public void isFinal_trueForDelivered() {
        assertTrue(OrderStatus.DELIVERED.isFinal());
    }

    @Test
    public void isFinal_trueForCancelled() {
        assertTrue(OrderStatus.CANCELLED.isFinal());
    }

    @Test
    public void isFinal_falseForOthers() {
        assertFalse(OrderStatus.PENDING.isFinal());
        assertFalse(OrderStatus.CONFIRMED.isFinal());
        assertFalse(OrderStatus.PROCESSING.isFinal());
        assertFalse(OrderStatus.SHIPPED.isFinal());
    }

    @Test
    public void isCancellable_trueOnlyForPending() {
        assertTrue(OrderStatus.PENDING.isCancellable());
        assertFalse(OrderStatus.CONFIRMED.isCancellable());
        assertFalse(OrderStatus.PROCESSING.isCancellable());
        assertFalse(OrderStatus.SHIPPED.isCancellable());
        assertFalse(OrderStatus.DELIVERED.isCancellable());
        assertFalse(OrderStatus.CANCELLED.isCancellable());
    }
}
