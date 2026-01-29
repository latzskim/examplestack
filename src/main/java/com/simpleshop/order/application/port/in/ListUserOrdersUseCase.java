package com.simpleshop.order.application.port.in;

import com.simpleshop.order.application.query.ListUserOrdersQuery;
import com.simpleshop.order.application.query.OrderSummaryView;
import org.springframework.data.domain.Page;

public interface ListUserOrdersUseCase {
    Page<OrderSummaryView> execute(ListUserOrdersQuery query);
}
