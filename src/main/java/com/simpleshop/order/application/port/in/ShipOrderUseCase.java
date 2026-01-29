package com.simpleshop.order.application.port.in;

import com.simpleshop.order.application.command.ShipOrderCommand;
import com.simpleshop.order.application.query.OrderView;

public interface ShipOrderUseCase {
    OrderView execute(ShipOrderCommand command);
}
