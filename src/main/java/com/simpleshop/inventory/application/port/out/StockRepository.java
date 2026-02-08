package com.simpleshop.inventory.application.port.out;

import com.simpleshop.inventory.domain.model.Stock;
import com.simpleshop.inventory.domain.model.vo.StockId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository {
    Optional<Stock> findById(StockId id);
    Optional<Stock> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);
    List<Stock> findByProductId(UUID productId);
    List<Stock> findByProductIds(Collection<UUID> productIds);
    Page<Stock> findByWarehouseId(UUID warehouseId, Pageable pageable);
    Stock save(Stock stock);
    int sumAvailableByProductId(UUID productId);
    int sumReservedByProductId(UUID productId);
    Map<UUID, Integer> sumAvailableByProductIds(Collection<UUID> productIds);
    Map<UUID, Integer> sumReservedByProductIds(Collection<UUID> productIds);
}
