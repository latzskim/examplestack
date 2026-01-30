package com.simpleshop.shipping.domain.model.vo;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ShipmentStatusTest {

    @Test
    public void canTransitionTo_createdToPicked() {
        assertTrue(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.PICKED));
    }

    @Test
    public void canTransitionTo_createdToFailed() {
        assertTrue(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.FAILED));
    }

    @Test
    public void canTransitionTo_pickedToPacked() {
        assertTrue(ShipmentStatus.PICKED.canTransitionTo(ShipmentStatus.PACKED));
    }

    @Test
    public void canTransitionTo_pickedToFailed() {
        assertTrue(ShipmentStatus.PICKED.canTransitionTo(ShipmentStatus.FAILED));
    }

    @Test
    public void canTransitionTo_packedToShipped() {
        assertTrue(ShipmentStatus.PACKED.canTransitionTo(ShipmentStatus.SHIPPED));
    }

    @Test
    public void canTransitionTo_shippedToInTransit() {
        assertTrue(ShipmentStatus.SHIPPED.canTransitionTo(ShipmentStatus.IN_TRANSIT));
    }

    @Test
    public void canTransitionTo_inTransitToOutForDelivery() {
        assertTrue(ShipmentStatus.IN_TRANSIT.canTransitionTo(ShipmentStatus.OUT_FOR_DELIVERY));
    }

    @Test
    public void canTransitionTo_outForDeliveryToDelivered() {
        assertTrue(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.DELIVERED));
    }

    @Test
    public void canTransitionTo_outForDeliveryToFailed() {
        assertTrue(ShipmentStatus.OUT_FOR_DELIVERY.canTransitionTo(ShipmentStatus.FAILED));
    }

    @Test
    public void canTransitionTo_deliveredToAnything_notAllowed() {
        assertFalse(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.SHIPPED));
        assertFalse(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.FAILED));
        assertFalse(ShipmentStatus.DELIVERED.canTransitionTo(ShipmentStatus.IN_TRANSIT));
    }

    @Test
    public void canTransitionTo_failedToAnything_notAllowed() {
        assertFalse(ShipmentStatus.FAILED.canTransitionTo(ShipmentStatus.PICKED));
        assertFalse(ShipmentStatus.FAILED.canTransitionTo(ShipmentStatus.SHIPPED));
        assertFalse(ShipmentStatus.FAILED.canTransitionTo(ShipmentStatus.DELIVERED));
    }

    @Test
    public void canTransitionTo_skipSteps_notAllowed() {
        // Can't skip steps
        assertFalse(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.DELIVERED));
        assertFalse(ShipmentStatus.CREATED.canTransitionTo(ShipmentStatus.SHIPPED));
        assertFalse(ShipmentStatus.PICKED.canTransitionTo(ShipmentStatus.DELIVERED));
    }

    @Test
    public void isFinal_trueForDelivered() {
        assertTrue(ShipmentStatus.DELIVERED.isFinal());
    }

    @Test
    public void isFinal_trueForFailed() {
        assertTrue(ShipmentStatus.FAILED.isFinal());
    }

    @Test
    public void isFinal_falseForOthers() {
        assertFalse(ShipmentStatus.CREATED.isFinal());
        assertFalse(ShipmentStatus.PICKED.isFinal());
        assertFalse(ShipmentStatus.PACKED.isFinal());
        assertFalse(ShipmentStatus.SHIPPED.isFinal());
        assertFalse(ShipmentStatus.IN_TRANSIT.isFinal());
        assertFalse(ShipmentStatus.OUT_FOR_DELIVERY.isFinal());
    }

    @Test
    public void getDisplayName_returnsCorrectNames() {
        assertEquals(ShipmentStatus.CREATED.getDisplayName(), "Created");
        assertEquals(ShipmentStatus.PICKED.getDisplayName(), "Picked");
        assertEquals(ShipmentStatus.PACKED.getDisplayName(), "Packed");
        assertEquals(ShipmentStatus.SHIPPED.getDisplayName(), "Shipped");
        assertEquals(ShipmentStatus.IN_TRANSIT.getDisplayName(), "In Transit");
        assertEquals(ShipmentStatus.OUT_FOR_DELIVERY.getDisplayName(), "Out for Delivery");
        assertEquals(ShipmentStatus.DELIVERED.getDisplayName(), "Delivered");
        assertEquals(ShipmentStatus.FAILED.getDisplayName(), "Failed");
    }
}
