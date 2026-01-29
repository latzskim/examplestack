package com.simpleshop.order.application.port.in;

import com.simpleshop.order.application.query.GetOrderQuery;
import com.simpleshop.order.application.query.GetOrderByNumberQuery;
import com.simpleshop.order.application.query.OrderView;
import java.util.Optional;

public interface GetOrderUseCase {
    Optional<OrderView> execute(GetOrderQuery query);
    Optional<OrderView> execute(GetOrderByNumberQuery query);
}
