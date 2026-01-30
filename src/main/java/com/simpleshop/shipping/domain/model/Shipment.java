package com.simpleshop.shipping.domain.model;

import com.simpleshop.shared.domain.model.AggregateRoot;
import com.simpleshop.shared.domain.model.vo.Address;
import com.simpleshop.shipping.domain.event.ShipmentCreated;
import com.simpleshop.shipping.domain.event.ShipmentDelivered;
import com.simpleshop.shipping.domain.event.ShipmentFailed;
import com.simpleshop.shipping.domain.event.ShipmentStatusUpdated;
import com.simpleshop.shipping.domain.exception.InvalidShipmentStateException;
import com.simpleshop.shipping.domain.model.vo.ShipmentId;
import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import com.simpleshop.shipping.domain.model.vo.TrackingNumber;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments")
public class Shipment extends AggregateRoot<Shipment> {
    
    @Id
    private UUID id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "tracking_number", nullable = false, unique = true))
    private TrackingNumber trackingNumber;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "destination_street", nullable = false)),
        @AttributeOverride(name = "city", column = @Column(name = "destination_city", nullable = false)),
        @AttributeOverride(name = "postalCode", column = @Column(name = "destination_postal_code", nullable = false)),
        @AttributeOverride(name = "country", column = @Column(name = "destination_country", nullable = false))
    })
    private Address destinationAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;
    
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("changedAt DESC")
    private List<ShipmentStatusChange> statusHistory = new ArrayList<>();
    
    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    protected Shipment() {}
    
    private Shipment(TrackingNumber trackingNumber, UUID orderId, UUID warehouseId,
                     Address destinationAddress, LocalDate estimatedDelivery) {
        this.id = UUID.randomUUID();
        this.trackingNumber = trackingNumber;
        this.orderId = orderId;
        this.warehouseId = warehouseId;
        this.destinationAddress = destinationAddress;
        this.status = ShipmentStatus.CREATED;
        this.estimatedDelivery = estimatedDelivery;
        this.createdAt = Instant.now();
    }

    public static Shipment create(TrackingNumber trackingNumber, UUID orderId, UUID warehouseId,
                                   Address destinationAddress, LocalDate estimatedDelivery) {
        if (trackingNumber == null) throw new IllegalArgumentException("Tracking number cannot be null");
        if (orderId == null) throw new IllegalArgumentException("Order ID cannot be null");
        if (warehouseId == null) throw new IllegalArgumentException("Warehouse ID cannot be null");
        if (destinationAddress == null) throw new IllegalArgumentException("Destination address cannot be null");

        Shipment shipment = new Shipment(trackingNumber, orderId, warehouseId, destinationAddress, estimatedDelivery);

        // Add initial status change after object is fully constructed
        shipment.statusHistory.add(new ShipmentStatusChange(
            shipment, ShipmentStatus.CREATED, null, "Shipment created"
        ));

        shipment.registerEvent(new ShipmentCreated(
            shipment.id,
            shipment.trackingNumber.getValue(),
            shipment.orderId,
            shipment.warehouseId
        ));

        return shipment;
    }
    
    public void updateStatus(ShipmentStatus newStatus, String location, String notes) {
        if (status.isFinal()) {
            throw new InvalidShipmentStateException(status, newStatus);
        }
        
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidShipmentStateException(status, newStatus);
        }
        
        ShipmentStatus previousStatus = this.status;
        this.status = newStatus;
        
        this.statusHistory.add(new ShipmentStatusChange(
            this, newStatus, location, notes
        ));
        
        registerEvent(new ShipmentStatusUpdated(
            this.id,
            this.trackingNumber.getValue(),
            previousStatus,
            newStatus,
            location,
            notes
        ));
        
        if (newStatus == ShipmentStatus.DELIVERED) {
            registerEvent(new ShipmentDelivered(
                this.id,
                this.trackingNumber.getValue(),
                this.orderId
            ));
        }
    }
    
    public void markAsFailed(String reason) {
        if (status.isFinal()) {
            throw new InvalidShipmentStateException(status, ShipmentStatus.FAILED);
        }
        
        ShipmentStatus previousStatus = this.status;
        this.status = ShipmentStatus.FAILED;
        
        this.statusHistory.add(new ShipmentStatusChange(
            this, ShipmentStatus.FAILED, null, reason
        ));
        
        registerEvent(new ShipmentFailed(
            this.id,
            this.trackingNumber.getValue(),
            this.orderId,
            reason
        ));
    }
    
    public void markAsPicked(String location, String notes) {
        updateStatus(ShipmentStatus.PICKED, location, notes);
    }
    
    public void markAsPacked(String location, String notes) {
        updateStatus(ShipmentStatus.PACKED, location, notes);
    }
    
    public void markAsShipped(String location, String notes) {
        updateStatus(ShipmentStatus.SHIPPED, location, notes);
    }
    
    public void markAsInTransit(String location, String notes) {
        updateStatus(ShipmentStatus.IN_TRANSIT, location, notes);
    }
    
    public void markAsOutForDelivery(String location, String notes) {
        updateStatus(ShipmentStatus.OUT_FOR_DELIVERY, location, notes);
    }
    
    public void markAsDelivered(String location, String notes) {
        updateStatus(ShipmentStatus.DELIVERED, location, notes);
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public ShipmentId getShipmentId() {
        return ShipmentId.of(id);
    }
    
    public TrackingNumber getTrackingNumber() {
        return trackingNumber;
    }
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public UUID getWarehouseId() {
        return warehouseId;
    }
    
    public Address getDestinationAddress() {
        return destinationAddress;
    }
    
    public ShipmentStatus getStatus() {
        return status;
    }
    
    public List<ShipmentStatusChange> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }
    
    public LocalDate getEstimatedDelivery() {
        return estimatedDelivery;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
    }
    
    public boolean isFailed() {
        return status == ShipmentStatus.FAILED;
    }
    
    public boolean isFinal() {
        return status.isFinal();
    }
}
