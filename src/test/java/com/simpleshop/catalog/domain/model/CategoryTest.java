package com.simpleshop.catalog.domain.model;

import com.simpleshop.catalog.domain.event.CategoryCreated;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class CategoryTest {

    @Test
    public void create_createsCategoryWithAllFields() {
        UUID parentId = UUID.randomUUID();
        
        Category category = Category.create("Electronics", "Electronic devices", parentId, 10);
        
        assertNotNull(category.getCategoryId());
        assertEquals(category.getName(), "Electronics");
        assertEquals(category.getDescription(), "Electronic devices");
        assertEquals(category.getParentId(), parentId);
        assertEquals(category.getSortOrder(), 10);
        assertNotNull(category.getCreatedAt());
    }
    
    @Test
    public void create_allowsNullOptionalFields() {
        Category category = Category.create("Root Category", null, null, 0);
        
        assertNull(category.getDescription());
        assertNull(category.getParentId());
        assertEquals(category.getSortOrder(), 0);
    }
    
    @Test
    public void create_publishesCategoryCreatedEvent() {
        Category category = Category.create("Test Category", null, null, 0);
        
        var events = category.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof CategoryCreated);
        
        CategoryCreated event = (CategoryCreated) events.iterator().next();
        assertEquals(event.getCategoryId(), category.getCategoryId());
        assertEquals(event.getName(), "Test Category");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsExceptionForNullName() {
        Category.create(null, null, null, 0);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsExceptionForBlankName() {
        Category.create("   ", null, null, 0);
    }
    
    @Test
    public void update_updatesAllFields() {
        Category category = Category.create("Original", "Original Desc", null, 0);
        UUID newParentId = UUID.randomUUID();
        
        category.update("Updated", "Updated Desc", newParentId, 5);
        
        assertEquals(category.getName(), "Updated");
        assertEquals(category.getDescription(), "Updated Desc");
        assertEquals(category.getParentId(), newParentId);
        assertEquals(category.getSortOrder(), 5);
    }
}
