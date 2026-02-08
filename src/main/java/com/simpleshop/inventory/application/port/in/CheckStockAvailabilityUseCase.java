package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.query.CheckStockAvailabilityQuery;
import com.simpleshop.inventory.application.query.ProductAvailabilityView;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface CheckStockAvailabilityUseCase {
    ProductAvailabilityView check(CheckStockAvailabilityQuery query);
    Map<UUID, ProductAvailabilityView> checkMany(Collection<UUID> productIds);
}
