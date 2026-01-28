package com.simpleshop.catalog.domain.model;

import com.simpleshop.catalog.domain.event.ProductCreated;
import com.simpleshop.catalog.domain.event.ProductDeactivated;
import com.simpleshop.catalog.domain.event.ProductUpdated;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.catalog.domain.model.vo.Sku;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.testng.Assert.*;

public class ProductTest {

    @Test
    public void create_createsActiveProductWithAllFields() {
        Sku sku = Sku.of("TEST-SKU-001");
        Money price = Money.usd(new BigDecimal("29.99"));
        UUID categoryId = UUID.randomUUID();
        
        Product product = Product.create("Test Product", "Description", sku, price, categoryId, "https://example.com/image.jpg");
        
        assertNotNull(product.getProductId());
        assertEquals(product.getName(), "Test Product");
        assertEquals(product.getDescription(), "Description");
        assertEquals(product.getSku(), sku);
        assertEquals(product.getPrice(), price);
        assertEquals(product.getCategoryId(), categoryId);
        assertEquals(product.getImageUrl(), "https://example.com/image.jpg");
        assertTrue(product.isActive());
        assertNotNull(product.getCreatedAt());
        assertNotNull(product.getUpdatedAt());
    }
    
    @Test
    public void create_allowsNullOptionalFields() {
        Sku sku = Sku.of("TEST-SKU-002");
        Money price = Money.usd(new BigDecimal("19.99"));
        
        Product product = Product.create("Test Product", null, sku, price, null, null);
        
        assertNull(product.getDescription());
        assertNull(product.getCategoryId());
        assertNull(product.getImageUrl());
    }
    
    @Test
    public void create_publishesProductCreatedEvent() {
        Sku sku = Sku.of("EVENT-SKU-001");
        Product product = Product.create("Event Product", null, sku, Money.usd(BigDecimal.TEN), null, null);
        
        var events = product.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ProductCreated);
        
        ProductCreated event = (ProductCreated) events.iterator().next();
        assertEquals(event.getProductId(), product.getProductId());
        assertEquals(event.getName(), "Event Product");
        assertEquals(event.getSku(), sku);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsExceptionForNullName() {
        Product.create(null, null, Sku.of("SKU"), Money.usd(BigDecimal.TEN), null, null);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsExceptionForBlankName() {
        Product.create("   ", null, Sku.of("SKU"), Money.usd(BigDecimal.TEN), null, null);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsExceptionForNullSku() {
        Product.create("Test", null, null, Money.usd(BigDecimal.TEN), null, null);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsExceptionForNullPrice() {
        Product.create("Test", null, Sku.of("SKU"), null, null, null);
    }
    
    @Test
    public void update_updatesFieldsAndPublishesEvent() {
        Product product = createTestProduct();
        product.clearEvents();
        
        Money newPrice = Money.of(new BigDecimal("39.99"), "EUR");
        UUID newCategoryId = UUID.randomUUID();
        
        product.update("Updated Name", "New Description", newPrice, newCategoryId, "https://new-image.jpg");
        
        assertEquals(product.getName(), "Updated Name");
        assertEquals(product.getDescription(), "New Description");
        assertEquals(product.getPrice(), newPrice);
        assertEquals(product.getCategoryId(), newCategoryId);
        assertEquals(product.getImageUrl(), "https://new-image.jpg");
        
        var events = product.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ProductUpdated);
    }
    
    @Test
    public void deactivate_setsActiveToFalseAndPublishesEvent() {
        Product product = createTestProduct();
        assertTrue(product.isActive());
        product.clearEvents();
        
        product.deactivate();
        
        assertFalse(product.isActive());
        var events = product.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ProductDeactivated);
    }
    
    @Test
    public void deactivate_isIdempotent() {
        Product product = createTestProduct();
        product.deactivate();
        product.clearEvents();
        
        product.deactivate();
        
        assertFalse(product.isActive());
        assertTrue(product.getDomainEvents().isEmpty());
    }
    
    @Test
    public void activate_setsActiveToTrue() {
        Product product = createTestProduct();
        product.deactivate();
        
        product.activate();
        
        assertTrue(product.isActive());
    }
    
    private Product createTestProduct() {
        return Product.create(
            "Test Product",
            "Test Description",
            Sku.of("TEST-SKU"),
            Money.usd(new BigDecimal("29.99")),
            null,
            null
        );
    }
}
