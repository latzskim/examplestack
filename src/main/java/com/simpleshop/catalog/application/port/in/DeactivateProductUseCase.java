package com.simpleshop.catalog.application.port.in;

import java.util.UUID;

public interface DeactivateProductUseCase {
    void deactivate(UUID productId);
}
