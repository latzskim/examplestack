package com.simpleshop.shipping.application.port.in;

import com.simpleshop.shipping.application.query.ShipmentTrackingView;
import java.util.Optional;

public interface TrackShipmentUseCase {
    Optional<ShipmentTrackingView> trackShipment(String trackingNumber);
}
