package com.simpleshop.inventory.domain.model;

import com.simpleshop.inventory.domain.event.WarehouseCreated;
import com.simpleshop.inventory.domain.model.vo.WarehouseId;
import com.simpleshop.inventory.domain.model.vo.WarehouseName;
import com.simpleshop.shared.domain.model.AggregateRoot;
import com.simpleshop.shared.domain.model.vo.Address;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "warehouses")
public class Warehouse extends AggregateRoot<Warehouse> {
    
    @Id
    private UUID id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false, length = 100))
    private WarehouseName name;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "warehouse_street", nullable = false)),
        @AttributeOverride(name = "city", column = @Column(name = "warehouse_city", nullable = false)),
        @AttributeOverride(name = "postalCode", column = @Column(name = "warehouse_postal_code", nullable = false)),
        @AttributeOverride(name = "country", column = @Column(name = "warehouse_country", nullable = false))
    })
    private Address address;
    
    @Column(name = "active", nullable = false)
    private boolean active;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    protected Warehouse() {}
    
    private Warehouse(WarehouseId id, WarehouseName name, Address address) {
        this.id = id.getValue();
        this.name = name;
        this.address = address;
        this.active = true;
        this.createdAt = Instant.now();
    }
    
    public static Warehouse create(String name, Address address) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Warehouse name cannot be empty");
        if (address == null) throw new IllegalArgumentException("Address cannot be null");
        
        Warehouse warehouse = new Warehouse(WarehouseId.generate(), WarehouseName.of(name), address);
        warehouse.registerEvent(new WarehouseCreated(warehouse.getWarehouseId()));
        return warehouse;
    }
    
    public void update(String name, Address address) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Warehouse name cannot be empty");
        if (address == null) throw new IllegalArgumentException("Address cannot be null");
        
        this.name = WarehouseName.of(name);
        this.address = address;
    }
    
    public void deactivate() {
        if (!this.active) return;
        this.active = false;
    }
    
    public void activate() {
        this.active = true;
    }
    
    public WarehouseId getWarehouseId() {
        return WarehouseId.of(id);
    }
    
    public UUID getId() {
        return id;
    }
    
    public WarehouseName getName() {
        return name;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
}
