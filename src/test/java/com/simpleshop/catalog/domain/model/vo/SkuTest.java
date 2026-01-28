package com.simpleshop.catalog.domain.model.vo;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SkuTest {

    @Test
    public void of_createsSkuWithUppercaseValue() {
        Sku sku = Sku.of("test-sku-123");
        
        assertEquals(sku.getValue(), "TEST-SKU-123");
    }
    
    @Test
    public void of_acceptsAlphanumericWithDashesAndUnderscores() {
        Sku sku1 = Sku.of("ABC123");
        Sku sku2 = Sku.of("ABC-123");
        Sku sku3 = Sku.of("ABC_123");
        Sku sku4 = Sku.of("A-B_C-1_2_3");
        
        assertEquals(sku1.getValue(), "ABC123");
        assertEquals(sku2.getValue(), "ABC-123");
        assertEquals(sku3.getValue(), "ABC_123");
        assertEquals(sku4.getValue(), "A-B_C-1_2_3");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForNullValue() {
        Sku.of(null);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForBlankValue() {
        Sku.of("   ");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForSpecialCharacters() {
        Sku.of("SKU@123");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForSpaces() {
        Sku.of("SKU 123");
    }
    
    @Test
    public void equals_returnsTrueForSameValue() {
        Sku sku1 = Sku.of("SKU-001");
        Sku sku2 = Sku.of("sku-001");
        
        assertEquals(sku1, sku2);
        assertEquals(sku1.hashCode(), sku2.hashCode());
    }
    
    @Test
    public void equals_returnsFalseForDifferentValue() {
        Sku sku1 = Sku.of("SKU-001");
        Sku sku2 = Sku.of("SKU-002");
        
        assertNotEquals(sku1, sku2);
    }
    
    @Test
    public void toString_returnsValue() {
        Sku sku = Sku.of("test-sku");
        
        assertEquals(sku.toString(), "TEST-SKU");
    }
}
