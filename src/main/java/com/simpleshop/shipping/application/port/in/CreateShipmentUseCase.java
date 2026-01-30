package com.simpleshop.shipping.application.port.in;

import com.simpleshop.shipping.application.command.CreateShipmentCommand;
import com.simpleshop.shipping.application.query.ShipmentView;

public interface CreateShipmentUseCase {
    ShipmentView createShipment(CreateShipmentCommand command);
}
