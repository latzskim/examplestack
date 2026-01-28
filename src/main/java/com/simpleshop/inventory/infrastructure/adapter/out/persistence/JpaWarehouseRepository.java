package com.simpleshop.inventory.infrastructure.adapter.out.persistence;

import com.simpleshop.inventory.domain.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaWarehouseRepository extends JpaRepository<Warehouse, UUID> {
}
