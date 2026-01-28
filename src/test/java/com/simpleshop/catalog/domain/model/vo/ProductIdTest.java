package com.simpleshop.catalog.domain.model.vo;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class ProductIdTest {

    @Test
    public void generate_createsProductIdWithRandomUuid() {
        ProductId id1 = ProductId.generate();
        ProductId id2 = ProductId.generate();
        
        assertNotNull(id1.getValue());
        assertNotNull(id2.getValue());
        assertNotEquals(id1, id2);
    }
    
    @Test
    public void of_createsProductIdWithGivenUuid() {
        UUID uuid = UUID.randomUUID();
        
        ProductId id = ProductId.of(uuid);
        
        assertEquals(id.getValue(), uuid);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForNullUuid() {
        ProductId.of(null);
    }
    
    @Test
    public void equals_returnsTrueForSameUuid() {
        UUID uuid = UUID.randomUUID();
        ProductId id1 = ProductId.of(uuid);
        ProductId id2 = ProductId.of(uuid);
        
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
    
    @Test
    public void equals_returnsFalseForDifferentUuid() {
        ProductId id1 = ProductId.generate();
        ProductId id2 = ProductId.generate();
        
        assertNotEquals(id1, id2);
    }
    
    @Test
    public void toString_returnsUuidString() {
        UUID uuid = UUID.randomUUID();
        ProductId id = ProductId.of(uuid);
        
        assertEquals(id.toString(), uuid.toString());
    }
}
