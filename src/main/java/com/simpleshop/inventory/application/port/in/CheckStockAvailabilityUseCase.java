package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.query.CheckStockAvailabilityQuery;
import com.simpleshop.inventory.application.query.ProductAvailabilityView;

public interface CheckStockAvailabilityUseCase {
    ProductAvailabilityView check(CheckStockAvailabilityQuery query);
}
