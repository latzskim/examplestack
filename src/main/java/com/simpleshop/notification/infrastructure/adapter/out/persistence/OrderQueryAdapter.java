package com.simpleshop.notification.infrastructure.adapter.out.persistence;

import com.simpleshop.notification.application.port.out.OrderQueryPort;
import com.simpleshop.order.application.port.in.GetOrderUseCase;
import com.simpleshop.order.application.query.GetOrderQuery;
import com.simpleshop.order.application.query.OrderView;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrderQueryAdapter implements OrderQueryPort {
    
    private final GetOrderUseCase getOrderUseCase;
    
    public OrderQueryAdapter(GetOrderUseCase getOrderUseCase) {
        this.getOrderUseCase = getOrderUseCase;
    }
    
    @Override
    public Optional<OrderView> getOrderById(UUID orderId) {
        return getOrderUseCase.execute(new GetOrderQuery(orderId));
    }
}
