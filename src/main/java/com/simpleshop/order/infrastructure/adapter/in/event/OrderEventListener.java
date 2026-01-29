package com.simpleshop.order.infrastructure.adapter.in.event;

import com.simpleshop.inventory.application.command.ReleaseStockCommand;
import com.simpleshop.inventory.application.command.ReserveStockCommand;
import com.simpleshop.inventory.application.port.in.ReleaseStockUseCase;
import com.simpleshop.inventory.application.port.in.ReserveStockUseCase;
import com.simpleshop.order.application.port.in.GetOrderUseCase;
import com.simpleshop.order.application.query.GetOrderQuery;
import com.simpleshop.order.application.query.OrderItemView;
import com.simpleshop.order.application.query.OrderView;
import com.simpleshop.order.domain.event.OrderCancelled;
import com.simpleshop.order.domain.event.OrderConfirmed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;
    private final GetOrderUseCase getOrderUseCase;
    
    public OrderEventListener(ReserveStockUseCase reserveStockUseCase,
                              ReleaseStockUseCase releaseStockUseCase,
                              GetOrderUseCase getOrderUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.releaseStockUseCase = releaseStockUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }
    
    @ApplicationModuleListener
    public void onOrderConfirmed(OrderConfirmed event) {
        log.info("Order confirmed: {} - reserving stock", event.getOrderNumber());
        
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(event.getOrderId()))
            .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getOrderId()));
        
        for (OrderItemView item : order.items()) {
            UUID warehouseId = item.warehouseId() != null ? item.warehouseId() : getDefaultWarehouseId();
            
            try {
                reserveStockUseCase.reserve(new ReserveStockCommand(
                    item.productId(),
                    warehouseId,
                    item.quantity()
                ));
                log.debug("Reserved {} units of product {} from warehouse {} for order {}", 
                    item.quantity(), item.productId(), warehouseId, event.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to reserve stock for product {} in order {}: {}", 
                    item.productId(), event.getOrderNumber(), e.getMessage());
            }
        }
    }
    
    @ApplicationModuleListener
    public void onOrderCancelled(OrderCancelled event) {
        log.info("Order cancelled: {} - releasing reserved stock", event.getOrderNumber());
        
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(event.getOrderId()))
            .orElseThrow(() -> new IllegalStateException("Order not found: " + event.getOrderId()));
        
        for (OrderItemView item : order.items()) {
            UUID warehouseId = item.warehouseId() != null ? item.warehouseId() : getDefaultWarehouseId();
            
            try {
                releaseStockUseCase.release(new ReleaseStockCommand(
                    item.productId(),
                    warehouseId,
                    item.quantity()
                ));
                log.debug("Released {} units of product {} to warehouse {} from order {}", 
                    item.quantity(), item.productId(), warehouseId, event.getOrderNumber());
            } catch (Exception e) {
                log.warn("Failed to release stock for product {} from order {}: {}", 
                    item.productId(), event.getOrderNumber(), e.getMessage());
            }
        }
    }

    private UUID getDefaultWarehouseId() {
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
