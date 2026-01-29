package com.simpleshop.cart.application.query;

import java.math.BigDecimal;

public record CartSummaryView(
    int itemCount,
    BigDecimal total,
    String currency
) {}
