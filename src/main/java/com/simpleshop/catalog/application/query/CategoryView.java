package com.simpleshop.catalog.application.query;

import java.util.UUID;

public record CategoryView(
    UUID id,
    String name,
    String description,
    UUID parentId,
    String parentName,
    int sortOrder
) {}
