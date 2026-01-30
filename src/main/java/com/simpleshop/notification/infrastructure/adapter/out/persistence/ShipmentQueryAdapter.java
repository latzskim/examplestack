package com.simpleshop.notification.infrastructure.adapter.out.persistence;

import com.simpleshop.notification.application.port.out.ShipmentQueryPort;
import com.simpleshop.shipping.application.port.in.GetShipmentUseCase;
import com.simpleshop.shipping.application.port.in.ListShipmentsByOrderUseCase;
import com.simpleshop.shipping.application.query.ShipmentView;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ShipmentQueryAdapter implements ShipmentQueryPort {
    
    private final GetShipmentUseCase getShipmentUseCase;
    private final ListShipmentsByOrderUseCase listShipmentsByOrderUseCase;
    
    public ShipmentQueryAdapter(GetShipmentUseCase getShipmentUseCase,
                                ListShipmentsByOrderUseCase listShipmentsByOrderUseCase) {
        this.getShipmentUseCase = getShipmentUseCase;
        this.listShipmentsByOrderUseCase = listShipmentsByOrderUseCase;
    }
    
    @Override
    public Optional<ShipmentView> getShipmentById(UUID shipmentId) {
        return getShipmentUseCase.getShipment(shipmentId);
    }
    
    @Override
    public List<ShipmentView> getShipmentsByOrderId(UUID orderId) {
        return listShipmentsByOrderUseCase.listShipmentsByOrder(orderId, Pageable.unpaged()).getContent();
    }
}
