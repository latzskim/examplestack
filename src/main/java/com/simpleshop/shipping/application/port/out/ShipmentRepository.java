package com.simpleshop.shipping.application.port.out;

import com.simpleshop.shipping.domain.model.Shipment;
import com.simpleshop.shipping.domain.model.vo.ShipmentId;
import com.simpleshop.shipping.domain.model.vo.TrackingNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(ShipmentId id);
    Optional<Shipment> findByTrackingNumber(TrackingNumber trackingNumber);
    Page<Shipment> findByOrderId(UUID orderId, Pageable pageable);
}
