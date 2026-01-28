package com.simpleshop.inventory.domain.model;

import com.simpleshop.inventory.domain.event.WarehouseCreated;
import com.simpleshop.shared.domain.model.vo.Address;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class WarehouseTest {
    
    @Test
    public void shouldCreateWarehouse() {
        Address address = Address.of("123 Main St", "Boston", "02101", "USA");
        Warehouse warehouse = Warehouse.create("Main Warehouse", address);
        
        assertNotNull(warehouse.getId());
        assertEquals(warehouse.getName().getValue(), "Main Warehouse");
        assertEquals(warehouse.getAddress(), address);
        assertTrue(warehouse.isActive());
        assertNotNull(warehouse.getCreatedAt());
    }
    
    @Test
    public void shouldRegisterWarehouseCreatedEvent() {
        Address address = Address.of("123 Main St", "Boston", "02101", "USA");
        Warehouse warehouse = Warehouse.create("Main Warehouse", address);
        
        assertEquals(warehouse.getDomainEvents().size(), 1);
        assertTrue(warehouse.getDomainEvents().iterator().next() instanceof WarehouseCreated);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectEmptyName() {
        Address address = Address.of("123 Main St", "Boston", "02101", "USA");
        Warehouse.create("", address);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectNullAddress() {
        Warehouse.create("Main Warehouse", null);
    }
    
    @Test
    public void shouldDeactivateWarehouse() {
        Address address = Address.of("123 Main St", "Boston", "02101", "USA");
        Warehouse warehouse = Warehouse.create("Main Warehouse", address);
        warehouse.clearEvents();
        
        warehouse.deactivate();
        
        assertFalse(warehouse.isActive());
    }
    
    @Test
    public void shouldUpdateWarehouse() {
        Address address = Address.of("123 Main St", "Boston", "02101", "USA");
        Warehouse warehouse = Warehouse.create("Main Warehouse", address);
        
        Address newAddress = Address.of("456 Oak Ave", "New York", "10001", "USA");
        warehouse.update("Updated Warehouse", newAddress);
        
        assertEquals(warehouse.getName().getValue(), "Updated Warehouse");
        assertEquals(warehouse.getAddress(), newAddress);
    }
}
