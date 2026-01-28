package com.simpleshop.catalog.application.command;

import java.util.UUID;

public record CreateCategoryCommand(
    String name,
    String description,
    UUID parentId,
    Integer sortOrder
) {
    public CreateCategoryCommand {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required");
        if (sortOrder == null) sortOrder = 0;
    }
}
