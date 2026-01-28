package com.simpleshop.catalog.application.query;

import java.util.UUID;

public record ListCategoriesQuery(UUID parentId) {
    public static ListCategoriesQuery root() {
        return new ListCategoriesQuery(null);
    }
}
