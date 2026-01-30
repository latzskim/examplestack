package com.simpleshop.order.application.port.out;

import com.simpleshop.order.domain.model.vo.OrderNumber;

/**
 * Port for generating unique order numbers.
 * Implementation should use a persistent sequence to ensure uniqueness across restarts.
 */
public interface OrderNumberGenerator {
    OrderNumber generate();
}
