package com.simpleshop.catalog.application.port.in;

import com.simpleshop.catalog.application.query.CategoryView;
import com.simpleshop.catalog.application.query.GetCategoryQuery;
import java.util.Optional;

public interface GetCategoryUseCase {
    Optional<CategoryView> get(GetCategoryQuery query);
}
