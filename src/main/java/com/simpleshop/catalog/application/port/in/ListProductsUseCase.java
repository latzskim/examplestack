package com.simpleshop.catalog.application.port.in;

import com.simpleshop.catalog.application.query.ListProductsQuery;
import com.simpleshop.catalog.application.query.ProductListView;
import org.springframework.data.domain.Page;

public interface ListProductsUseCase {
    Page<ProductListView> list(ListProductsQuery query);
}
