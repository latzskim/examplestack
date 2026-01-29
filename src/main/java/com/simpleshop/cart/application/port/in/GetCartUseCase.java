package com.simpleshop.cart.application.port.in;

import com.simpleshop.cart.application.query.CartView;
import com.simpleshop.cart.application.query.GetCartQuery;

public interface GetCartUseCase {
    CartView execute(GetCartQuery query);
}
