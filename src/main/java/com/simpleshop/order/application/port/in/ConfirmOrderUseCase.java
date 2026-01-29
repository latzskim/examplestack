package com.simpleshop.order.application.port.in;

import com.simpleshop.order.application.command.ConfirmOrderCommand;
import com.simpleshop.order.application.query.OrderView;

public interface ConfirmOrderUseCase {
    OrderView execute(ConfirmOrderCommand command);
}
