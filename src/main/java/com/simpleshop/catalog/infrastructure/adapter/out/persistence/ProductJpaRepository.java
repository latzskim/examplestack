package com.simpleshop.catalog.infrastructure.adapter.out.persistence;

import com.simpleshop.catalog.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
    
    @Query("SELECT p FROM Product p WHERE p.sku.value = :skuValue")
    Optional<Product> findBySkuValue(@Param("skuValue") String skuValue);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.sku.value = :skuValue")
    boolean existsBySkuValue(@Param("skuValue") String skuValue);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:activeOnly IS NULL OR :activeOnly = false OR p.active = true)")
    Page<Product> findAllWithFilters(
        @Param("categoryId") UUID categoryId,
        @Param("activeOnly") Boolean activeOnly,
        Pageable pageable
    );
}
