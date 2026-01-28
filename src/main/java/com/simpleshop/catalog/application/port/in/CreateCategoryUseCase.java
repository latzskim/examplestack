package com.simpleshop.catalog.application.port.in;

import com.simpleshop.catalog.application.command.CreateCategoryCommand;
import com.simpleshop.catalog.application.query.CategoryView;

public interface CreateCategoryUseCase {
    CategoryView create(CreateCategoryCommand command);
}
