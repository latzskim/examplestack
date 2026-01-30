package com.simpleshop.inventory.application.port.in;

import com.simpleshop.inventory.application.command.AllocateStockCommand;
import com.simpleshop.inventory.application.query.StockAllocationResult;

/**
 * Allocates and reserves stock for order items.
 * Finds the best warehouse for each product and reserves the stock atomically.
 * Throws InsufficientStockException if any product cannot be fulfilled.
 */
public interface AllocateStockUseCase {
    StockAllocationResult allocate(AllocateStockCommand command);
}
