package com.simpleshop.notification.application.port.out;

import com.simpleshop.order.application.query.OrderView;
import java.util.Optional;
import java.util.UUID;

public interface OrderQueryPort {
    
    Optional<OrderView> getOrderById(UUID orderId);
}
