package com.simpleshop.catalog.application.service;

import com.simpleshop.catalog.application.command.CreateCategoryCommand;
import com.simpleshop.catalog.application.port.in.CreateCategoryUseCase;
import com.simpleshop.catalog.application.port.in.GetCategoryUseCase;
import com.simpleshop.catalog.application.port.in.ListCategoriesUseCase;
import com.simpleshop.catalog.application.port.out.CategoryRepository;
import com.simpleshop.catalog.application.query.CategoryView;
import com.simpleshop.catalog.application.query.GetCategoryQuery;
import com.simpleshop.catalog.application.query.ListCategoriesQuery;
import com.simpleshop.catalog.domain.exception.CategoryNotFoundException;
import com.simpleshop.catalog.domain.model.Category;
import com.simpleshop.catalog.domain.model.vo.CategoryId;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService implements CreateCategoryUseCase, GetCategoryUseCase, ListCategoriesUseCase {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    @WithSpan("catalog.createCategory")
    public CategoryView create(CreateCategoryCommand command) {
        if (command.parentId() != null) {
            categoryRepository.findById(CategoryId.of(command.parentId()))
                .orElseThrow(() -> new CategoryNotFoundException(command.parentId()));
        }
        
        Category category = Category.create(
            command.name(),
            command.description(),
            command.parentId(),
            command.sortOrder()
        );
        
        category = categoryRepository.save(category);
        return toCategoryView(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("catalog.getCategory")
    public Optional<CategoryView> get(GetCategoryQuery query) {
        return categoryRepository.findById(CategoryId.of(query.categoryId()))
            .map(this::toCategoryView);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("catalog.listCategories")
    public List<CategoryView> list(ListCategoriesQuery query) {
        List<Category> categories;
        if (query.parentId() != null) {
            categories = categoryRepository.findByParentId(query.parentId());
        } else {
            categories = categoryRepository.findByParentId(null);
        }

        List<UUID> parentIds = categories.stream()
            .map(Category::getParentId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<UUID, String> parentNames = categoryRepository.findByIds(parentIds).stream()
            .collect(Collectors.toMap(Category::getId, Category::getName, (left, right) -> left));

        return categories.stream()
            .map(category -> toCategoryView(category, parentNames.get(category.getParentId())))
            .toList();
    }
    
    private CategoryView toCategoryView(Category category) {
        String parentName = category.getParentId() == null ? null :
            categoryRepository.findById(CategoryId.of(category.getParentId()))
                .map(Category::getName)
                .orElse(null);

        return toCategoryView(category, parentName);
    }

    private CategoryView toCategoryView(Category category, String parentName) {
        return new CategoryView(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getParentId(),
            parentName,
            category.getSortOrder()
        );
    }
}
