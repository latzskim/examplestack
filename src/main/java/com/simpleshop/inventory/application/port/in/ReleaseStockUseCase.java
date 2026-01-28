package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.command.ReleaseStockCommand;

public interface ReleaseStockUseCase {
    void release(ReleaseStockCommand command);
}
