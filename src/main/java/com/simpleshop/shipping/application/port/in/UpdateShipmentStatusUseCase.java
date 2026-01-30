package com.simpleshop.shipping.application.port.in;

import com.simpleshop.shipping.application.command.UpdateShipmentStatusCommand;
import com.simpleshop.shipping.application.query.ShipmentView;

public interface UpdateShipmentStatusUseCase {
    ShipmentView updateStatus(UpdateShipmentStatusCommand command);
}
