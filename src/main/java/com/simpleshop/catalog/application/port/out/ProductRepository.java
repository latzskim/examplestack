package com.simpleshop.catalog.application.port.out;

import com.simpleshop.catalog.domain.model.Product;
import com.simpleshop.catalog.domain.model.vo.ProductId;
import com.simpleshop.catalog.domain.model.vo.Sku;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    Optional<Product> findBySku(Sku sku);
    boolean existsBySku(Sku sku);
    Page<Product> findAll(UUID categoryId, Boolean activeOnly, Pageable pageable);
}
