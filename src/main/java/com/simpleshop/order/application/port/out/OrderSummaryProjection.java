package com.simpleshop.order.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryProjection(
    UUID id,
    String orderNumber,
    String status,
    BigDecimal totalAmount,
    String currency,
    int itemCount,
    Instant createdAt
) {}
