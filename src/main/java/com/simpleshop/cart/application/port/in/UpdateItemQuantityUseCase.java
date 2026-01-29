package com.simpleshop.cart.application.port.in;

import com.simpleshop.cart.application.command.UpdateItemQuantityCommand;
import com.simpleshop.cart.application.query.CartView;

public interface UpdateItemQuantityUseCase {
    CartView execute(UpdateItemQuantityCommand command);
}
