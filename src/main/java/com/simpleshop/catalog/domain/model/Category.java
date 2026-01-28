package com.simpleshop.catalog.domain.model;

import com.simpleshop.catalog.domain.event.CategoryCreated;
import com.simpleshop.catalog.domain.model.vo.CategoryId;
import com.simpleshop.shared.domain.model.AggregateRoot;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category extends AggregateRoot<Category> {
    
    @Id
    private UUID id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "parent_id")
    private UUID parentId;
    
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    protected Category() {}
    
    private Category(CategoryId id, String name, String description, UUID parentId, int sortOrder) {
        this.id = id.getValue();
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
        this.createdAt = Instant.now();
    }
    
    public static Category create(String name, String description, UUID parentId, int sortOrder) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name cannot be empty");
        
        Category category = new Category(CategoryId.generate(), name, description, parentId, sortOrder);
        category.registerEvent(new CategoryCreated(category.getCategoryId(), category.name));
        return category;
    }
    
    public void update(String name, String description, UUID parentId, int sortOrder) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name cannot be empty");
        
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
    }
    
    public CategoryId getCategoryId() {
        return CategoryId.of(id);
    }
    
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public UUID getParentId() {
        return parentId;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
}
