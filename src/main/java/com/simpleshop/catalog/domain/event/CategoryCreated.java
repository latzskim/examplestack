package com.simpleshop.catalog.domain.event;

import com.simpleshop.catalog.domain.model.vo.CategoryId;
import com.simpleshop.shared.domain.model.DomainEvent;

public class CategoryCreated extends DomainEvent {
    
    private final CategoryId categoryId;
    private final String name;
    
    public CategoryCreated(CategoryId categoryId, String name) {
        super();
        this.categoryId = categoryId;
        this.name = name;
    }
    
    public CategoryId getCategoryId() {
        return categoryId;
    }
    
    public String getName() {
        return name;
    }
}
