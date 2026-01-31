package com.simpleshop.inventory.infrastructure.adapter.out.persistence;

import com.simpleshop.inventory.application.port.out.StockRepository;
import com.simpleshop.inventory.domain.model.Stock;
import com.simpleshop.inventory.domain.model.vo.StockId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
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
}
