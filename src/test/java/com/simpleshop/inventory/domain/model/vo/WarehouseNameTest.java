package com.simpleshop.inventory.domain.model.vo;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class WarehouseNameTest {

    @Test
    public void shouldCreateWarehouseName() {
        WarehouseName name = WarehouseName.of("Main Warehouse");
        
        assertNotNull(name);
        assertEquals(name.getValue(), "Main Warehouse");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectNullValue() {
        WarehouseName.of(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectEmptyValue() {
        WarehouseName.of("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectBlankValue() {
        WarehouseName.of("   ");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectTooLongValue() {
        String longName = "A".repeat(101);
        WarehouseName.of(longName);
    }

    @Test
    public void shouldAcceptMaxLengthValue() {
        String maxLengthName = "A".repeat(100);
        WarehouseName name = WarehouseName.of(maxLengthName);
        
        assertNotNull(name);
        assertEquals(name.getValue().length(), 100);
    }

    @Test
    public void shouldBeEqualWhenSameValue() {
        WarehouseName name1 = WarehouseName.of("Main Warehouse");
        WarehouseName name2 = WarehouseName.of("Main Warehouse");
        
        assertEquals(name1, name2);
        assertEquals(name1.hashCode(), name2.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenDifferentValue() {
        WarehouseName name1 = WarehouseName.of("Main Warehouse");
        WarehouseName name2 = WarehouseName.of("Secondary Warehouse");
        
        assertNotEquals(name1, name2);
    }
}
