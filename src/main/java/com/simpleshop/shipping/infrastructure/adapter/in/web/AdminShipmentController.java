package com.simpleshop.shipping.infrastructure.adapter.in.web;

import com.simpleshop.shipping.application.command.UpdateShipmentStatusCommand;
import com.simpleshop.shipping.application.port.in.GetShipmentUseCase;
import com.simpleshop.shipping.application.port.in.ListShipmentsByOrderUseCase;
import com.simpleshop.shipping.application.port.in.UpdateShipmentStatusUseCase;
import com.simpleshop.shipping.application.query.ShipmentView;
import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin/shipments")
public class AdminShipmentController {
    
    private final GetShipmentUseCase getShipmentUseCase;
    private final ListShipmentsByOrderUseCase listShipmentsByOrderUseCase;
    private final UpdateShipmentStatusUseCase updateShipmentStatusUseCase;
    
    public AdminShipmentController(GetShipmentUseCase getShipmentUseCase,
                                    ListShipmentsByOrderUseCase listShipmentsByOrderUseCase,
                                    UpdateShipmentStatusUseCase updateShipmentStatusUseCase) {
        this.getShipmentUseCase = getShipmentUseCase;
        this.listShipmentsByOrderUseCase = listShipmentsByOrderUseCase;
        this.updateShipmentStatusUseCase = updateShipmentStatusUseCase;
    }
    
    @GetMapping("/order/{orderId}")
    public String listShipmentsByOrder(@PathVariable UUID orderId, Pageable pageable, Model model) {
        Page<ShipmentView> shipments = listShipmentsByOrderUseCase.listShipmentsByOrder(orderId, pageable);
        model.addAttribute("shipments", shipments);
        model.addAttribute("orderId", orderId);
        return "admin/shipments/list";
    }
    
    @PostMapping("/{shipmentId}/status")
    public String updateStatus(@PathVariable UUID shipmentId,
                                @RequestParam ShipmentStatus newStatus,
                                @RequestParam(required = false) String location,
                                @RequestParam(required = false) String notes,
                                @RequestParam UUID orderId) {
        UpdateShipmentStatusCommand command = new UpdateShipmentStatusCommand(
            shipmentId, newStatus, location, notes
        );
        updateShipmentStatusUseCase.updateStatus(command);
        
        return "redirect:/admin/shipments/order/" + orderId;
    }
}
