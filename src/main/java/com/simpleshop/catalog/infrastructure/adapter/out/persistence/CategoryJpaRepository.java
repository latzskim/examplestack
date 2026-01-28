package com.simpleshop.catalog.infrastructure.adapter.out.persistence;

import com.simpleshop.catalog.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {
    
    List<Category> findByParentIdOrderBySortOrder(UUID parentId);
    
    List<Category> findByParentIdIsNullOrderBySortOrder();
}
