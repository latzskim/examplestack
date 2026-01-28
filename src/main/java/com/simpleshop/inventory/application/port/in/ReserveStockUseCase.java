package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.command.ReserveStockCommand;

public interface ReserveStockUseCase {
    void reserve(ReserveStockCommand command);
}
