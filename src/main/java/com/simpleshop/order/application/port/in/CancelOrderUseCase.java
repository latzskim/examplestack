package com.simpleshop.order.application.port.in;

import com.simpleshop.order.application.command.CancelOrderCommand;

public interface CancelOrderUseCase {
    void execute(CancelOrderCommand command);
}
