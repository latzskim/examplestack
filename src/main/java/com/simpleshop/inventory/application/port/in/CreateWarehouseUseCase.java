package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.command.CreateWarehouseCommand;
import com.simpleshop.inventory.application.query.WarehouseView;

public interface CreateWarehouseUseCase {
    WarehouseView create(CreateWarehouseCommand command);
}
