package com.simpleshop.cart.application.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartView(
    UUID id,
    List<CartItemView> items,
    BigDecimal total,
    String currency,
    int itemCount
) {}
