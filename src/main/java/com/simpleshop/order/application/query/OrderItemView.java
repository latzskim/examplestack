package com.simpleshop.order.application.query;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemView(
    UUID id,
    UUID productId,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    String currency,
    BigDecimal subtotal,
    UUID warehouseId
) {}
