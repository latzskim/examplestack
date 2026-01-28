package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.query.GetWarehouseStockQuery;
import com.simpleshop.inventory.application.query.StockView;
import org.springframework.data.domain.Page;

public interface ListWarehouseStockUseCase {
    Page<StockView> list(GetWarehouseStockQuery query);
}
