package com.simpleshop.inventory.domain.model.vo;

import org.testng.annotations.Test;
import java.util.UUID;
import static org.testng.Assert.*;

public class WarehouseIdTest {

    @Test
    public void shouldGenerateWarehouseId() {
        WarehouseId warehouseId = WarehouseId.generate();
        
        assertNotNull(warehouseId);
        assertNotNull(warehouseId.getValue());
    }

    @Test
    public void shouldCreateWarehouseIdFromUuid() {
        UUID uuid = UUID.randomUUID();
        WarehouseId warehouseId = WarehouseId.of(uuid);
        
        assertNotNull(warehouseId);
        assertEquals(warehouseId.getValue(), uuid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectNullValue() {
        WarehouseId.of(null);
    }

    @Test
    public void shouldBeEqualWhenSameValue() {
        UUID uuid = UUID.randomUUID();
        WarehouseId id1 = WarehouseId.of(uuid);
        WarehouseId id2 = WarehouseId.of(uuid);
        
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenDifferentValue() {
        WarehouseId id1 = WarehouseId.generate();
        WarehouseId id2 = WarehouseId.generate();
        
        assertNotEquals(id1, id2);
    }
}
