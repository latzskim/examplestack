package com.simpleshop.inventory.infrastructure.adapter.out.persistence;

import com.simpleshop.inventory.application.port.out.WarehouseRepository;
import com.simpleshop.inventory.domain.model.Warehouse;
import com.simpleshop.inventory.domain.model.vo.WarehouseId;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class WarehouseRepositoryAdapter implements WarehouseRepository {
    
    private final JpaWarehouseRepository jpaRepository;
    
    public WarehouseRepositoryAdapter(JpaWarehouseRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Optional<Warehouse> findById(WarehouseId id) {
        return jpaRepository.findById(id.getValue());
    }

    @Override
    public List<Warehouse> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        return jpaRepository.save(warehouse);
    }

    @Override
    public boolean existsById(WarehouseId id) {
        return jpaRepository.existsById(id.getValue());
    }
}
