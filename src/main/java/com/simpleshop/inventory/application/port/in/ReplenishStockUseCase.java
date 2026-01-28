package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.command.ReplenishStockCommand;
import com.simpleshop.inventory.application.query.StockView;

public interface ReplenishStockUseCase {
    StockView replenish(ReplenishStockCommand command);
}
