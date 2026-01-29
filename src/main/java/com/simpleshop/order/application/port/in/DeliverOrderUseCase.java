package com.simpleshop.order.application.port.in;

import com.simpleshop.order.application.command.DeliverOrderCommand;
import com.simpleshop.order.application.query.OrderView;

public interface DeliverOrderUseCase {
    OrderView execute(DeliverOrderCommand command);
}
