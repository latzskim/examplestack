package com.simpleshop.shipping.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment_status_history")
public class ShipmentStatusChange {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.simpleshop.shipping.domain.model.vo.ShipmentStatus status;

    @Column(nullable = false)
    private Instant changedAt;

    @Column(length = 200)
    private String location;

    @Column(length = 1000)
    private String notes;

    protected ShipmentStatusChange() {}

    public ShipmentStatusChange(Shipment shipment,
                                com.simpleshop.shipping.domain.model.vo.ShipmentStatus status,
                                String location, String notes) {
        this.id = UUID.randomUUID();
        this.shipment = shipment;
        this.status = status;
        this.changedAt = Instant.now();
        this.location = location;
        this.notes = notes;
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getShipmentId() {
        return shipment != null ? shipment.getId() : null;
    }
    
    public com.simpleshop.shipping.domain.model.vo.ShipmentStatus getStatus() {
        return status;
    }
    
    public Instant getChangedAt() {
        return changedAt;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getNotes() {
        return notes;
    }
}
