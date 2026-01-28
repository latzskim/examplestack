package com.simpleshop.catalog.application.port.in;

import com.simpleshop.catalog.application.query.CategoryView;
import com.simpleshop.catalog.application.query.ListCategoriesQuery;
import java.util.List;

public interface ListCategoriesUseCase {
    List<CategoryView> list(ListCategoriesQuery query);
}
