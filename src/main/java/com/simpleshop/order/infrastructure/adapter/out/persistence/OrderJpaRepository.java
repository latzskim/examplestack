package com.simpleshop.order.infrastructure.adapter.out.persistence;

import com.simpleshop.order.domain.model.Order;
import com.simpleshop.order.domain.model.vo.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    interface OrderSummaryJpaProjection {
        UUID getId();
        String getOrderNumber();
        OrderStatus getStatus();
        BigDecimal getTotalAmount();
        String getCurrency();
        Long getItemCount();
        Instant getCreatedAt();
    }
    
    @Query("SELECT o FROM Order o WHERE o.orderNumber.value = :orderNumber")
    Optional<Order> findByOrderNumberValue(@Param("orderNumber") String orderNumber);
    
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query(
        value = """
            SELECT o.id AS id,
                   o.orderNumber.value AS orderNumber,
                   o.status AS status,
                   o.totalAmount.amount AS totalAmount,
                   o.totalAmount.currencyCode AS currency,
                   COALESCE(SUM(i.quantity.value), 0) AS itemCount,
                   o.createdAt AS createdAt
            FROM Order o
            LEFT JOIN o.items i
            WHERE o.userId = :userId
            GROUP BY o.id, o.orderNumber.value, o.status, o.totalAmount.amount, o.totalAmount.currencyCode, o.createdAt
            ORDER BY o.createdAt DESC
            """,
        countQuery = "SELECT COUNT(o) FROM Order o WHERE o.userId = :userId"
    )
    Page<OrderSummaryJpaProjection> findOrderSummariesByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
