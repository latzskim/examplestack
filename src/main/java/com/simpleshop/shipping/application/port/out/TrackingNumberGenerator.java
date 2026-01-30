package com.simpleshop.shipping.application.port.out;

import com.simpleshop.shipping.domain.model.vo.TrackingNumber;

/**
 * Port for generating unique tracking numbers.
 * Implementation should use a persistent sequence to ensure uniqueness across restarts.
 */
public interface TrackingNumberGenerator {
    TrackingNumber generate();
}
