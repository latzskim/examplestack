package com.simpleshop.catalog.application.port.in;

import com.simpleshop.catalog.application.command.UpdateProductCommand;
import com.simpleshop.catalog.application.query.ProductView;

public interface UpdateProductUseCase {
    ProductView update(UpdateProductCommand command);
}
