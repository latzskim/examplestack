package com.simpleshop.order.application.port.in;

import com.simpleshop.order.application.command.PlaceOrderFromCartCommand;
import com.simpleshop.order.application.query.OrderView;

public interface PlaceOrderFromCartUseCase {
    OrderView execute(PlaceOrderFromCartCommand command);
}
