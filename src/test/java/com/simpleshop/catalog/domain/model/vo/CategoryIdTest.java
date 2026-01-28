package com.simpleshop.catalog.domain.model.vo;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class CategoryIdTest {

    @Test
    public void generate_createsCategoryIdWithRandomUuid() {
        CategoryId id1 = CategoryId.generate();
        CategoryId id2 = CategoryId.generate();
        
        assertNotNull(id1.getValue());
        assertNotNull(id2.getValue());
        assertNotEquals(id1, id2);
    }
    
    @Test
    public void of_createsCategoryIdWithGivenUuid() {
        UUID uuid = UUID.randomUUID();
        
        CategoryId id = CategoryId.of(uuid);
        
        assertEquals(id.getValue(), uuid);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForNullUuid() {
        CategoryId.of(null);
    }
    
    @Test
    public void equals_returnsTrueForSameUuid() {
        UUID uuid = UUID.randomUUID();
        CategoryId id1 = CategoryId.of(uuid);
        CategoryId id2 = CategoryId.of(uuid);
        
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
    
    @Test
    public void equals_returnsFalseForDifferentUuid() {
        CategoryId id1 = CategoryId.generate();
        CategoryId id2 = CategoryId.generate();
        
        assertNotEquals(id1, id2);
    }
    
    @Test
    public void toString_returnsUuidString() {
        UUID uuid = UUID.randomUUID();
        CategoryId id = CategoryId.of(uuid);
        
        assertEquals(id.toString(), uuid.toString());
    }
}
