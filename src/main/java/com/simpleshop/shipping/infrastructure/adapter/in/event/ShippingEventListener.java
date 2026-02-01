package com.simpleshop.shipping.infrastructure.adapter.in.event;

import com.simpleshop.order.application.port.in.GetOrderUseCase;
import com.simpleshop.order.application.query.GetOrderQuery;
import com.simpleshop.order.application.query.OrderItemView;
import com.simpleshop.order.application.query.OrderView;
import com.simpleshop.order.domain.event.OrderConfirmed;
import com.simpleshop.shared.domain.model.vo.Address;
import com.simpleshop.shipping.application.command.CreateShipmentCommand;
import com.simpleshop.shipping.application.port.in.CreateShipmentUseCase;
import com.simpleshop.shipping.application.query.ShipmentView;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles order lifecycle events for shipping management.
 * 
 * Shipping flow:
 * - When order is CONFIRMED, shipments are created for each warehouse
 * - Each shipment contains items from a single warehouse
 * - Tracking numbers are generated for each shipment
 */
@Component
public class ShippingEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(ShippingEventListener.class);
    
    private final GetOrderUseCase getOrderUseCase;
    private final CreateShipmentUseCase createShipmentUseCase;
    
    public ShippingEventListener(GetOrderUseCase getOrderUseCase,
                                  CreateShipmentUseCase createShipmentUseCase) {
        this.getOrderUseCase = getOrderUseCase;
        this.createShipmentUseCase = createShipmentUseCase;
    }
    
    @ApplicationModuleListener
    @WithSpan
    public void onOrderConfirmed(OrderConfirmed event) {
        log.info("Order confirmed: {} - creating shipments", event.getOrderNumber());
        
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(event.getOrderId()))
            .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getOrderId()));
        
        // Group items by warehouse and create a shipment per warehouse
        Set<UUID> warehouseIds = order.items().stream()
            .map(OrderItemView::warehouseId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        if (warehouseIds.isEmpty()) {
            log.warn("Order {} has no items with warehouse assignments, cannot create shipments", 
                event.getOrderNumber());
            return;
        }
        
        Address destinationAddress = Address.of(
            order.shippingStreet(),
            order.shippingCity(),
            order.shippingPostalCode(),
            order.shippingCountry()
        );
        
        // Estimated delivery is 3-5 days from now (business logic placeholder)
        LocalDate estimatedDelivery = LocalDate.now().plusDays(5);
        
        for (UUID warehouseId : warehouseIds) {
            try {
                CreateShipmentCommand command = new CreateShipmentCommand(
                    order.id(),
                    warehouseId,
                    destinationAddress,
                    estimatedDelivery
                );
                
                ShipmentView shipment = createShipmentUseCase.createShipment(command);
                log.info("Created shipment {} for order {} from warehouse {}", 
                    shipment.trackingNumber(), event.getOrderNumber(), warehouseId);
            } catch (Exception e) {
                log.error("Failed to create shipment for order {} from warehouse {}: {}", 
                    event.getOrderNumber(), warehouseId, e.getMessage());
            }
        }
    }
}
