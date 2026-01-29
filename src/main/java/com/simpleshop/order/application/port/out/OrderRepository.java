package com.simpleshop.order.application.port.out;

import com.simpleshop.order.domain.model.Order;
import com.simpleshop.order.domain.model.vo.OrderId;
import com.simpleshop.order.domain.model.vo.OrderNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    Optional<Order> findByOrderNumber(OrderNumber orderNumber);
    Page<Order> findByUserId(UUID userId, Pageable pageable);
    Page<Order> findAll(Pageable pageable);
}
