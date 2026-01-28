package com.simpleshop.catalog.application.port.in;

import java.util.UUID;

public interface ActivateProductUseCase {
    void activate(UUID productId);
}
