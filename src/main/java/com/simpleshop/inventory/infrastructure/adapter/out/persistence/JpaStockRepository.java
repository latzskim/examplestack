package com.simpleshop.inventory.infrastructure.adapter.out.persistence;

import com.simpleshop.inventory.domain.model.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaStockRepository extends JpaRepository<Stock, UUID> {

    interface ProductStockTotal {
        UUID getProductId();
        Long getTotal();
    }

    Optional<Stock> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);
    List<Stock> findByProductId(UUID productId);
    List<Stock> findByProductIdIn(Collection<UUID> productIds);
    Page<Stock> findByWarehouseId(UUID warehouseId, Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(s.quantity.value - s.reservedQuantity.value), 0) FROM Stock s WHERE s.productId = :productId")
    int sumAvailableByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT COALESCE(SUM(s.reservedQuantity.value), 0) FROM Stock s WHERE s.productId = :productId")
    int sumReservedByProductId(@Param("productId") UUID productId);

    @Query("SELECT s.productId AS productId, COALESCE(SUM(s.quantity.value - s.reservedQuantity.value), 0) AS total " +
           "FROM Stock s WHERE s.productId IN :productIds GROUP BY s.productId")
    List<ProductStockTotal> sumAvailableByProductIds(@Param("productIds") Collection<UUID> productIds);

    @Query("SELECT s.productId AS productId, COALESCE(SUM(s.reservedQuantity.value), 0) AS total " +
           "FROM Stock s WHERE s.productId IN :productIds GROUP BY s.productId")
    List<ProductStockTotal> sumReservedByProductIds(@Param("productIds") Collection<UUID> productIds);
}
