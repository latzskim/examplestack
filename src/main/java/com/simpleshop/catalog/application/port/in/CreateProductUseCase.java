package com.simpleshop.catalog.application.port.in;

import com.simpleshop.catalog.application.command.CreateProductCommand;
import com.simpleshop.catalog.application.query.ProductView;

public interface CreateProductUseCase {
    ProductView create(CreateProductCommand command);
}
