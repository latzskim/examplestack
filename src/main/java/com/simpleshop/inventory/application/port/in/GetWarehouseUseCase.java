package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.query.GetWarehouseQuery;
import com.simpleshop.inventory.application.query.WarehouseView;
import java.util.Optional;

public interface GetWarehouseUseCase {
    Optional<WarehouseView> get(GetWarehouseQuery query);
}
