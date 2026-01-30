package com.simpleshop.shipping.application.port.in;

import com.simpleshop.shipping.application.query.ShipmentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ListShipmentsByOrderUseCase {
    Page<ShipmentView> listShipmentsByOrder(UUID orderId, Pageable pageable);
}
