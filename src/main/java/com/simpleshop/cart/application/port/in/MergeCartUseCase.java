package com.simpleshop.cart.application.port.in;

import com.simpleshop.cart.application.command.MergeCartCommand;
import com.simpleshop.cart.application.query.CartView;

public interface MergeCartUseCase {
    CartView execute(MergeCartCommand command);
}
