package com.simpleshop.catalog.infrastructure.adapter.out.persistence;

import com.simpleshop.catalog.application.port.out.CategoryRepository;
import com.simpleshop.catalog.domain.model.Category;
import com.simpleshop.catalog.domain.model.vo.CategoryId;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepository {
    
    private final CategoryJpaRepository jpaRepository;
    
    public CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id.getValue());
    }

    @Override
    public List<Category> findByIds(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllById(ids);
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        if (parentId == null) {
            return jpaRepository.findByParentIdIsNullOrderBySortOrder();
        }
        return jpaRepository.findByParentIdOrderBySortOrder(parentId);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll();
    }
}
