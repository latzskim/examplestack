package com.simpleshop.cart.application.port.in;

import com.simpleshop.cart.application.command.RemoveItemFromCartCommand;
import com.simpleshop.cart.application.query.CartView;

public interface RemoveItemFromCartUseCase {
    CartView execute(RemoveItemFromCartCommand command);
}
