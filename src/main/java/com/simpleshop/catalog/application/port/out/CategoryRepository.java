package com.simpleshop.catalog.application.port.out;

import com.simpleshop.catalog.domain.model.Category;
import com.simpleshop.catalog.domain.model.vo.CategoryId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(CategoryId id);
    List<Category> findByParentId(UUID parentId);
    List<Category> findAll();
}
