package com.simpleshop.order.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryView(
    UUID id,
    String orderNumber,
    String status,
    BigDecimal totalAmount,
    String currency,
    int itemCount,
    Instant createdAt
) {}
