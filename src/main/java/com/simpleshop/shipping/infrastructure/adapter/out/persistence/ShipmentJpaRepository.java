package com.simpleshop.shipping.infrastructure.adapter.out.persistence;

import com.simpleshop.shipping.domain.model.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentJpaRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByTrackingNumberValue(String trackingNumber);
    Page<Shipment> findByOrderIdOrderByCreatedAtDesc(UUID orderId, Pageable pageable);
}
