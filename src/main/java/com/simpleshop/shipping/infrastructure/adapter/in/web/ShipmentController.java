package com.simpleshop.shipping.infrastructure.adapter.in.web;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import com.simpleshop.identity.infrastructure.security.ShopUserDetails;
import com.simpleshop.order.application.port.in.GetOrderUseCase;
import com.simpleshop.order.application.query.GetOrderQuery;
import com.simpleshop.shipping.application.port.in.TrackShipmentUseCase;
import com.simpleshop.shipping.application.query.ShipmentTrackingView;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/shipments")
public class ShipmentController {
    
    private final TrackShipmentUseCase trackShipmentUseCase;
    private final GetOrderUseCase getOrderUseCase;
    
    public ShipmentController(TrackShipmentUseCase trackShipmentUseCase,
                              GetOrderUseCase getOrderUseCase) {
        this.trackShipmentUseCase = trackShipmentUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }
    
    @GetMapping("/track")
    public String showTrackingForm() {
        return "shipments/track-form";
    }
    
    @PostMapping("/track")
    @WithSpan
    public String trackShipment(@RequestParam("trackingNumber") String trackingNumber,
                                @AuthenticationPrincipal ShopUserDetails user,
                                Model model) {
        Optional<ShipmentTrackingView> result = trackShipmentUseCase.trackShipment(trackingNumber);
        
        if (result.isPresent() && canAccessShipment(result.get(), user)) {
            model.addAttribute("tracking", result.orElseThrow());
            return "shipments/track-result";
        } else {
            model.addAttribute("error", "Tracking number not found");
            return "shipments/track-form";
        }
    }
    
    @GetMapping("/track/{trackingNumber}")
    @WithSpan
    public String trackShipmentByUrl(@PathVariable String trackingNumber,
                                     @AuthenticationPrincipal ShopUserDetails user,
                                     Model model) {
        Optional<ShipmentTrackingView> result = trackShipmentUseCase.trackShipment(trackingNumber);
        
        if (result.isPresent() && canAccessShipment(result.get(), user)) {
            model.addAttribute("tracking", result.orElseThrow());
            return "shipments/track-result";
        } else {
            model.addAttribute("error", "Tracking number not found");
            return "shipments/track-form";
        }
    }

    private boolean canAccessShipment(ShipmentTrackingView tracking, ShopUserDetails user) {
        if (user == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        return getOrderUseCase.execute(new GetOrderQuery(tracking.orderId()))
            .map(order -> order.userId().equals(user.getUserId().getValue()))
            .orElse(false);
    }
}
