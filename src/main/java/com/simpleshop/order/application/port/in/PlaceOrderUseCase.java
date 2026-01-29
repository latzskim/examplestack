package com.simpleshop.order.application.port.in;

import com.simpleshop.order.application.command.PlaceOrderCommand;
import com.simpleshop.order.application.query.OrderView;

public interface PlaceOrderUseCase {
    OrderView execute(PlaceOrderCommand command);
}
