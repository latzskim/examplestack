package com.simpleshop.cart.application.port.in;

import com.simpleshop.cart.application.command.ClearCartCommand;

public interface ClearCartUseCase {
    void execute(ClearCartCommand command);
}
