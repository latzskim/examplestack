package com.simpleshop.order.infrastructure.adapter.in.event;

import com.simpleshop.inventory.application.command.ConfirmStockReservationCommand;
import com.simpleshop.inventory.application.command.ReleaseStockCommand;
import com.simpleshop.inventory.application.port.in.ConfirmStockReservationUseCase;
import com.simpleshop.inventory.application.port.in.ReleaseStockUseCase;
import com.simpleshop.order.application.port.in.GetOrderUseCase;
import com.simpleshop.order.application.query.GetOrderQuery;
import com.simpleshop.order.application.query.OrderItemView;
import com.simpleshop.order.application.query.OrderView;
import com.simpleshop.order.domain.event.OrderCancelled;
import com.simpleshop.order.domain.event.OrderConfirmed;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Handles order lifecycle events for inventory management.
 * 
 * Stock reservation flow:
 * - Stock is RESERVED at order PLACEMENT time (in OrderService via AllocateStockUseCase)
 * - Stock is CONFIRMED (deducted from inventory) when order is CONFIRMED/PAID
 * - Stock is RELEASED when order is CANCELLED (only allowed from PENDING state)
 */
@Component
public class OrderEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final ReleaseStockUseCase releaseStockUseCase;
    private final ConfirmStockReservationUseCase confirmStockReservationUseCase;
    private final GetOrderUseCase getOrderUseCase;
    
    public OrderEventListener(ReleaseStockUseCase releaseStockUseCase,
                              ConfirmStockReservationUseCase confirmStockReservationUseCase,
                              GetOrderUseCase getOrderUseCase) {
        this.releaseStockUseCase = releaseStockUseCase;
        this.confirmStockReservationUseCase = confirmStockReservationUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }
    
    @ApplicationModuleListener
    @WithSpan
    public void onOrderCancelled(OrderCancelled event) {
        log.info("Order cancelled: {} - releasing reserved stock", event.getOrderNumber());
        
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(event.getOrderId()))
            .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getOrderId()));
        
        for (OrderItemView item : order.items()) {
            if (item.warehouseId() == null) {
                log.warn("OrderItem {} has no warehouseId, cannot release stock", item.id());
                continue;
            }
            
            try {
                releaseStockUseCase.release(new ReleaseStockCommand(
                    item.productId(),
                    item.warehouseId(),
                    item.quantity()
                ));
                log.debug("Released {} units of product {} to warehouse {} from order {}", 
                    item.quantity(), item.productId(), item.warehouseId(), event.getOrderNumber());
            } catch (Exception e) {
                log.warn("Failed to release stock for product {} from order {}: {}", 
                    item.productId(), event.getOrderNumber(), e.getMessage());
            }
        }
    }

    @ApplicationModuleListener
    @WithSpan
    public void onOrderConfirmed(OrderConfirmed event) {
        log.info("Order confirmed/paid: {} - confirming stock reservation (stock now sold)", event.getOrderNumber());
        
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(event.getOrderId()))
            .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getOrderId()));
        
        for (OrderItemView item : order.items()) {
            if (item.warehouseId() == null) {
                log.warn("OrderItem {} has no warehouseId, cannot confirm reservation", item.id());
                continue;
            }
            
            try {
                confirmStockReservationUseCase.confirm(new ConfirmStockReservationCommand(
                    item.productId(),
                    item.warehouseId(),
                    item.quantity()
                ));
                log.debug("Confirmed reservation of {} units of product {} from warehouse {} for order {}", 
                    item.quantity(), item.productId(), item.warehouseId(), event.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to confirm stock reservation for product {} in order {}: {}", 
                    item.productId(), event.getOrderNumber(), e.getMessage());
            }
        }
    }
}
