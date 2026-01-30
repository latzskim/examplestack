package com.simpleshop.shipping.domain.model;

import com.simpleshop.shipping.domain.event.*;
import com.simpleshop.shipping.domain.exception.InvalidShipmentStateException;
import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import com.simpleshop.shipping.domain.model.vo.TrackingNumber;
import com.simpleshop.shared.domain.model.vo.Address;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.*;

public class ShipmentTest {

    private UUID orderId;
    private UUID warehouseId;
    private Address destinationAddress;
    private LocalDate estimatedDelivery;

    @BeforeMethod
    public void setUp() {
        orderId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        destinationAddress = Address.of("456 Elm St", "Los Angeles", "90001", "USA");
        estimatedDelivery = LocalDate.now().plusDays(5);
    }

    @Test
    public void create_createsShipmentWithCorrectState() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);

        assertNotNull(shipment.getId());
        assertNotNull(shipment.getTrackingNumber());
        assertEquals(shipment.getOrderId(), orderId);
        assertEquals(shipment.getWarehouseId(), warehouseId);
        assertEquals(shipment.getDestinationAddress(), destinationAddress);
        assertEquals(shipment.getStatus(), ShipmentStatus.CREATED);
        assertEquals(shipment.getEstimatedDelivery(), estimatedDelivery);
        assertNotNull(shipment.getCreatedAt());
        assertNotNull(shipment.getStatusHistory());
        assertEquals(shipment.getStatusHistory().size(), 1);
    }

    @Test
    public void create_registersShipmentCreatedEvent() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);

        var events = shipment.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ShipmentCreated);

        ShipmentCreated event = (ShipmentCreated) events.iterator().next();
        assertEquals(event.getShipmentId(), shipment.getId());
        assertEquals(event.getTrackingNumber(), trackingNumber.getValue());
        assertEquals(event.getOrderId(), orderId);
        assertEquals(event.getWarehouseId(), warehouseId);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForNullTrackingNumber() {
        Shipment.create(null, orderId, warehouseId, destinationAddress, estimatedDelivery);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForNullOrderId() {
        Shipment.create(TrackingNumber.of("SHIP-2025-00001"), null, warehouseId, destinationAddress, estimatedDelivery);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForNullWarehouseId() {
        Shipment.create(TrackingNumber.of("SHIP-2025-00001"), orderId, null, destinationAddress, estimatedDelivery);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForNullDestinationAddress() {
        Shipment.create(TrackingNumber.of("SHIP-2025-00001"), orderId, warehouseId, null, estimatedDelivery);
    }

    @Test
    public void updateStatus_changesStatusCorrectly() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.clearEvents();

        shipment.updateStatus(ShipmentStatus.PICKED, "Warehouse A", "Items picked");

        assertEquals(shipment.getStatus(), ShipmentStatus.PICKED);
    }

    @Test
    public void updateStatus_addsStatusToHistory() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.clearEvents();

        shipment.updateStatus(ShipmentStatus.PICKED, "Warehouse A", "Items picked");

        assertEquals(shipment.getStatusHistory().size(), 2);
        ShipmentStatusChange latest = shipment.getStatusHistory().get(shipment.getStatusHistory().size() - 1);
        assertEquals(latest.getStatus(), ShipmentStatus.PICKED);
        assertEquals(latest.getLocation(), "Warehouse A");
        assertEquals(latest.getNotes(), "Items picked");
    }

    @Test
    public void updateStatus_registersShipmentStatusUpdatedEvent() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.clearEvents();

        shipment.updateStatus(ShipmentStatus.PICKED, "Warehouse A", "Items picked");

        var events = shipment.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ShipmentStatusUpdated);

        ShipmentStatusUpdated event = (ShipmentStatusUpdated) events.iterator().next();
        assertEquals(event.getShipmentId(), shipment.getId());
        assertEquals(event.getPreviousStatus(), ShipmentStatus.CREATED);
        assertEquals(event.getNewStatus(), ShipmentStatus.PICKED);
        assertEquals(event.getLocation(), "Warehouse A");
        assertEquals(event.getNotes(), "Items picked");
    }

    @Test
    public void updateStatus_toDelivered_registersShipmentDeliveredEvent() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.markAsPicked("Warehouse", null);
        shipment.markAsPacked("Warehouse", null);
        shipment.markAsShipped("Carrier Hub", null);
        shipment.markAsInTransit("Transit", null);
        shipment.markAsOutForDelivery("Local Hub", null);
        shipment.clearEvents();

        shipment.markAsDelivered("Customer Address", "Delivered successfully");

        boolean hasDeliveredEvent = shipment.getDomainEvents().stream()
            .anyMatch(e -> e instanceof ShipmentDelivered);
        assertTrue(hasDeliveredEvent);
    }

    @Test(expectedExceptions = InvalidShipmentStateException.class)
    public void updateStatus_throwsForInvalidTransition() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);

        // Can't go directly from CREATED to DELIVERED
        shipment.updateStatus(ShipmentStatus.DELIVERED, null, null);
    }

    @Test(expectedExceptions = InvalidShipmentStateException.class)
    public void updateStatus_throwsFromDeliveredStatus() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.markAsPicked(null, null);
        shipment.markAsPacked(null, null);
        shipment.markAsShipped(null, null);
        shipment.markAsInTransit(null, null);
        shipment.markAsOutForDelivery(null, null);
        shipment.markAsDelivered(null, null);

        shipment.updateStatus(ShipmentStatus.FAILED, null, null);
    }

    @Test(expectedExceptions = InvalidShipmentStateException.class)
    public void updateStatus_throwsFromFailedStatus() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.markAsFailed("Lost in transit");

        shipment.updateStatus(ShipmentStatus.PICKED, null, null);
    }

    @Test
    public void markAsFailed_changesStatusToFailed() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.clearEvents();

        shipment.markAsFailed("Package damaged");

        assertEquals(shipment.getStatus(), ShipmentStatus.FAILED);
    }

    @Test
    public void markAsFailed_registersShipmentFailedEvent() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.clearEvents();

        shipment.markAsFailed("Package damaged");

        var events = shipment.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ShipmentFailed);

        ShipmentFailed event = (ShipmentFailed) events.iterator().next();
        assertEquals(event.getShipmentId(), shipment.getId());
        assertEquals(event.getReason(), "Package damaged");
    }

    @Test
    public void isDelivered_returnsTrueForDeliveredStatus() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.markAsPicked(null, null);
        shipment.markAsPacked(null, null);
        shipment.markAsShipped(null, null);
        shipment.markAsInTransit(null, null);
        shipment.markAsOutForDelivery(null, null);
        shipment.markAsDelivered(null, null);

        assertTrue(shipment.isDelivered());
        assertFalse(shipment.isFailed());
        assertTrue(shipment.isFinal());
    }

    @Test
    public void isFailed_returnsTrueForFailedStatus() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);
        shipment.markAsFailed("Lost");

        assertFalse(shipment.isDelivered());
        assertTrue(shipment.isFailed());
        assertTrue(shipment.isFinal());
    }

    @Test
    public void fullLifecycle_successPath() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);

        assertEquals(shipment.getStatus(), ShipmentStatus.CREATED);
        assertFalse(shipment.isFinal());

        shipment.markAsPicked("Warehouse", "Picked from shelf");
        assertEquals(shipment.getStatus(), ShipmentStatus.PICKED);

        shipment.markAsPacked("Warehouse", "Packed in box");
        assertEquals(shipment.getStatus(), ShipmentStatus.PACKED);

        shipment.markAsShipped("Distribution Center", "Handed to carrier");
        assertEquals(shipment.getStatus(), ShipmentStatus.SHIPPED);

        shipment.markAsInTransit("Highway 101", "Moving to regional hub");
        assertEquals(shipment.getStatus(), ShipmentStatus.IN_TRANSIT);

        shipment.markAsOutForDelivery("Local Hub", "On delivery truck");
        assertEquals(shipment.getStatus(), ShipmentStatus.OUT_FOR_DELIVERY);

        shipment.markAsDelivered("Customer Door", "Signed by customer");
        assertEquals(shipment.getStatus(), ShipmentStatus.DELIVERED);
        assertTrue(shipment.isFinal());

        // Check full history
        List<ShipmentStatusChange> history = shipment.getStatusHistory();
        assertEquals(history.size(), 7);
    }

    @Test
    public void getStatusHistory_returnsUnmodifiableList() {
        TrackingNumber trackingNumber = TrackingNumber.of("SHIP-2025-00001");
        Shipment shipment = Shipment.create(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);

        assertThrows(UnsupportedOperationException.class, () -> {
            shipment.getStatusHistory().add(new ShipmentStatusChange(shipment, ShipmentStatus.FAILED, null, null));
        });
    }
}
