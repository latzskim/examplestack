package com.simpleshop.catalog.application.query;

import java.util.UUID;

public record GetCategoryQuery(UUID categoryId) {
    public GetCategoryQuery {
        if (categoryId == null) throw new IllegalArgumentException("Category ID is required");
    }
}
