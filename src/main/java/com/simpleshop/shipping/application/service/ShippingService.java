package com.simpleshop.shipping.application.service;

import com.simpleshop.shipping.application.command.CreateShipmentCommand;
import com.simpleshop.shipping.application.command.UpdateShipmentStatusCommand;
import com.simpleshop.shipping.application.port.in.*;
import com.simpleshop.shipping.application.port.out.ShipmentRepository;
import com.simpleshop.shipping.application.port.out.TrackingNumberGenerator;
import com.simpleshop.shipping.application.query.ShipmentTrackingView;
import com.simpleshop.shipping.application.query.ShipmentView;
import com.simpleshop.shipping.domain.exception.ShipmentNotFoundException;
import com.simpleshop.shipping.domain.model.Shipment;
import com.simpleshop.shipping.domain.model.vo.ShipmentId;
import com.simpleshop.shipping.domain.model.vo.TrackingNumber;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ShippingService implements
    CreateShipmentUseCase,
    UpdateShipmentStatusUseCase,
    GetShipmentUseCase,
    TrackShipmentUseCase,
    ListShipmentsByOrderUseCase {

    private final ShipmentRepository shipmentRepository;
    private final TrackingNumberGenerator trackingNumberGenerator;

    public ShippingService(ShipmentRepository shipmentRepository,
                          TrackingNumberGenerator trackingNumberGenerator) {
        this.shipmentRepository = shipmentRepository;
        this.trackingNumberGenerator = trackingNumberGenerator;
    }

    @Override
    @WithSpan("shipping.createShipment")
    public ShipmentView createShipment(CreateShipmentCommand command) {
        TrackingNumber trackingNumber = trackingNumberGenerator.generate();
        
        Shipment shipment = Shipment.create(
            trackingNumber,
            command.orderId(),
            command.warehouseId(),
            command.destinationAddress(),
            command.estimatedDelivery()
        );
        
        Shipment saved = shipmentRepository.save(shipment);
        return toShipmentView(saved);
    }
    
    @Override
    @WithSpan("shipping.updateStatus")
    public ShipmentView updateStatus(UpdateShipmentStatusCommand command) {
        Shipment shipment = shipmentRepository.findById(ShipmentId.of(command.shipmentId()))
            .orElseThrow(() -> new ShipmentNotFoundException(command.shipmentId()));
        
        shipment.updateStatus(command.newStatus(), command.location(), command.notes());
        
        Shipment saved = shipmentRepository.save(shipment);
        return toShipmentView(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("shipping.getShipment")
    public Optional<ShipmentView> getShipment(@SpanAttribute("shipmentId") UUID shipmentId) {
        return shipmentRepository.findById(ShipmentId.of(shipmentId))
            .map(this::toShipmentView);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("shipping.getShipmentByTrackingNumber")
    public Optional<ShipmentView> getShipmentByTrackingNumber(@SpanAttribute("trackingNumber") String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(TrackingNumber.of(trackingNumber))
            .map(this::toShipmentView);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("shipping.trackShipment")
    public Optional<ShipmentTrackingView> trackShipment(@SpanAttribute("trackingNumber") String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(TrackingNumber.of(trackingNumber))
            .map(this::toTrackingView);
    }
    
    @Override
    @Transactional(readOnly = true)
    @WithSpan("shipping.listShipmentsByOrder")
    public Page<ShipmentView> listShipmentsByOrder(@SpanAttribute("orderId") UUID orderId, Pageable pageable) {
        return shipmentRepository.findByOrderId(orderId, pageable)
            .map(this::toShipmentView);
    }
    
    private ShipmentView toShipmentView(Shipment shipment) {
        return new ShipmentView(
            shipment.getId(),
            shipment.getTrackingNumber().getValue(),
            shipment.getOrderId(),
            shipment.getWarehouseId(),
            shipment.getDestinationAddress().getStreet(),
            shipment.getDestinationAddress().getCity(),
            shipment.getDestinationAddress().getPostalCode(),
            shipment.getDestinationAddress().getCountry(),
            shipment.getStatus(),
            shipment.getEstimatedDelivery(),
            shipment.getCreatedAt()
        );
    }
    
    private ShipmentTrackingView toTrackingView(Shipment shipment) {
        String destinationAddress = String.format("%s, %s %s, %s",
            shipment.getDestinationAddress().getStreet(),
            shipment.getDestinationAddress().getCity(),
            shipment.getDestinationAddress().getPostalCode(),
            shipment.getDestinationAddress().getCountry()
        );
        
        var statusHistory = shipment.getStatusHistory().stream()
            .map(h -> new ShipmentTrackingView.ShipmentStatusHistoryView(
                h.getStatus(),
                h.getChangedAt(),
                h.getLocation(),
                h.getNotes()
            ))
            .toList();
        
        return new ShipmentTrackingView(
            shipment.getId(),
            shipment.getTrackingNumber().getValue(),
            shipment.getOrderId(),
            shipment.getStatus(),
            destinationAddress,
            statusHistory
        );
    }
}
