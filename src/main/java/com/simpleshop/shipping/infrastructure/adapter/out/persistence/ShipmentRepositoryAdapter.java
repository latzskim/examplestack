package com.simpleshop.shipping.infrastructure.adapter.out.persistence;

import com.simpleshop.shipping.application.port.out.ShipmentRepository;
import com.simpleshop.shipping.domain.model.Shipment;
import com.simpleshop.shipping.domain.model.vo.ShipmentId;
import com.simpleshop.shipping.domain.model.vo.TrackingNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ShipmentRepositoryAdapter implements ShipmentRepository {
    
    private final ShipmentJpaRepository jpaRepository;
    
    public ShipmentRepositoryAdapter(ShipmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Shipment save(Shipment shipment) {
        return jpaRepository.save(shipment);
    }
    
    @Override
    public Optional<Shipment> findById(ShipmentId id) {
        return jpaRepository.findById(id.getValue());
    }
    
    @Override
    public Optional<Shipment> findByTrackingNumber(TrackingNumber trackingNumber) {
        return jpaRepository.findByTrackingNumberValue(trackingNumber.getValue());
    }
    
    @Override
    public Page<Shipment> findByOrderId(UUID orderId, Pageable pageable) {
        return jpaRepository.findByOrderIdOrderByCreatedAtDesc(orderId, pageable);
    }
}
