package com.simpleshop.catalog.infrastructure.adapter.out.persistence;

import com.simpleshop.catalog.application.port.out.ProductRepository;
import com.simpleshop.catalog.domain.model.Product;
import com.simpleshop.catalog.domain.model.vo.ProductId;
import com.simpleshop.catalog.domain.model.vo.Sku;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {
    
    private final ProductJpaRepository jpaRepository;
    
    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }
    
    @Override
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.getValue());
    }
    
    @Override
    public Optional<Product> findBySku(Sku sku) {
        return jpaRepository.findBySkuValue(sku.getValue());
    }
    
    @Override
    public boolean existsBySku(Sku sku) {
        return jpaRepository.existsBySkuValue(sku.getValue());
    }
    
    @Override
    public Page<Product> findAll(UUID categoryId, Boolean activeOnly, Pageable pageable) {
        return jpaRepository.findAllWithFilters(categoryId, activeOnly, pageable);
    }
}
