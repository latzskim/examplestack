package com.simpleshop.cart.application.port.in;

import com.simpleshop.cart.application.command.AddItemToCartCommand;
import com.simpleshop.cart.application.query.CartView;

public interface AddItemToCartUseCase {
    CartView execute(AddItemToCartCommand command);
}
