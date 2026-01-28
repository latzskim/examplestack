package com.simpleshop.inventory.application.port.out;

import com.simpleshop.inventory.domain.model.Warehouse;
import com.simpleshop.inventory.domain.model.vo.WarehouseId;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository {
    Optional<Warehouse> findById(WarehouseId id);
    List<Warehouse> findAll();
    Warehouse save(Warehouse warehouse);
    boolean existsById(WarehouseId id);
}
