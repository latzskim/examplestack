package com.simpleshop.inventory.infrastructure.adapter.out.persistence;

import com.simpleshop.inventory.application.port.out.StockRepository;
import com.simpleshop.inventory.domain.model.Stock;
import com.simpleshop.inventory.domain.model.vo.StockId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StockRepositoryAdapter implements StockRepository {
    
    private final JpaStockRepository jpaRepository;
    
    public StockRepositoryAdapter(JpaStockRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<Stock> findById(StockId id) {
        return jpaRepository.findById(id.getValue());
    }

    @Override
    public Optional<Stock> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId) {
        return jpaRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    @Override
    public List<Stock> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<Stock> findByProductIds(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findByProductIdIn(productIds);
    }

    @Override
    public Page<Stock> findByWarehouseId(UUID warehouseId, Pageable pageable) {
        return jpaRepository.findByWarehouseId(warehouseId, pageable);
    }

    @Override
    public Stock save(Stock stock) {
        return jpaRepository.save(stock);
    }

    @Override
    public int sumAvailableByProductId(UUID productId) {
        return jpaRepository.sumAvailableByProductId(productId);
    }

    @Override
    public int sumReservedByProductId(UUID productId) {
        return jpaRepository.sumReservedByProductId(productId);
    }

    @Override
    public Map<UUID, Integer> sumAvailableByProductIds(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return toTotalsMap(jpaRepository.sumAvailableByProductIds(productIds));
    }

    @Override
    public Map<UUID, Integer> sumReservedByProductIds(Collection<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }
        return toTotalsMap(jpaRepository.sumReservedByProductIds(productIds));
    }

    private Map<UUID, Integer> toTotalsMap(List<JpaStockRepository.ProductStockTotal> totals) {
        Map<UUID, Integer> result = new LinkedHashMap<>();
        for (JpaStockRepository.ProductStockTotal row : totals) {
            long total = row.getTotal() == null ? 0L : row.getTotal();
            result.put(row.getProductId(), Math.toIntExact(total));
        }
        return result;
    }
}
