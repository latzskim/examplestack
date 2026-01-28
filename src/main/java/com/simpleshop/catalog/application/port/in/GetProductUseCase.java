package com.simpleshop.catalog.application.port.in;

import com.simpleshop.catalog.application.query.GetProductQuery;
import com.simpleshop.catalog.application.query.ProductView;
import java.util.Optional;

public interface GetProductUseCase {
    Optional<ProductView> get(GetProductQuery query);
}
