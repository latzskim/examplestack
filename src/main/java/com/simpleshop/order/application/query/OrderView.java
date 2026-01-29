package com.simpleshop.order.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderView(
    UUID id,
    String orderNumber,
    UUID userId,
    List<OrderItemView> items,
    String shippingStreet,
    String shippingCity,
    String shippingPostalCode,
    String shippingCountry,
    String status,
    BigDecimal totalAmount,
    String currency,
    int itemCount,
    Instant createdAt,
    Instant paidAt,
    Instant shippedAt,
    Instant deliveredAt,
    Instant cancelledAt,
    String cancellationReason
) {}
