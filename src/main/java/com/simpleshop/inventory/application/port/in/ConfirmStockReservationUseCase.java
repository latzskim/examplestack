package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.command.ConfirmStockReservationCommand;

/**
 * Confirms a stock reservation by permanently removing the quantity from inventory.
 * Called when an order is shipped and the items physically leave the warehouse.
 */
public interface ConfirmStockReservationUseCase {
    void confirm(ConfirmStockReservationCommand command);
}
