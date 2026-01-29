package com.simpleshop.cart.application.query;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemView(
    UUID productId,
    String productName,
    BigDecimal price,
    String currency,
    int quantity,
    BigDecimal subtotal
) {}
