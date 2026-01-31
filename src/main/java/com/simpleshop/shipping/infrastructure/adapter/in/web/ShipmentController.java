package com.simpleshop.shipping.infrastructure.adapter.in.web;

import io.micrometer.tracing.annotation.NewSpan;
import com.simpleshop.shipping.application.port.in.GetShipmentUseCase;
import com.simpleshop.shipping.application.port.in.TrackShipmentUseCase;
import com.simpleshop.shipping.application.query.ShipmentTrackingView;
import com.simpleshop.shipping.application.query.ShipmentView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/shipments")
public class ShipmentController {
    
    private final GetShipmentUseCase getShipmentUseCase;
    private final TrackShipmentUseCase trackShipmentUseCase;
    
    public ShipmentController(GetShipmentUseCase getShipmentUseCase, 
                               TrackShipmentUseCase trackShipmentUseCase) {
        this.getShipmentUseCase = getShipmentUseCase;
        this.trackShipmentUseCase = trackShipmentUseCase;
    }
    
    @GetMapping("/track")
    public String showTrackingForm() {
        return "shipments/track-form";
    }
    
    @PostMapping("/track")
    @NewSpan
    public String trackShipment(@RequestParam("trackingNumber") String trackingNumber, Model model) {
        Optional<ShipmentTrackingView> result = trackShipmentUseCase.trackShipment(trackingNumber);
        
        if (result.isPresent()) {
            model.addAttribute("tracking", result.get());
            return "shipments/track-result";
        } else {
            model.addAttribute("error", "Tracking number not found: " + trackingNumber);
            return "shipments/track-form";
        }
    }
    
    @GetMapping("/track/{trackingNumber}")
    @NewSpan
    public String trackShipmentByUrl(@PathVariable String trackingNumber, Model model) {
        Optional<ShipmentTrackingView> result = trackShipmentUseCase.trackShipment(trackingNumber);
        
        if (result.isPresent()) {
            model.addAttribute("tracking", result.get());
            return "shipments/track-result";
        } else {
            model.addAttribute("error", "Tracking number not found: " + trackingNumber);
            return "shipments/track-form";
        }
    }
}
