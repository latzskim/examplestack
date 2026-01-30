package com.simpleshop.order.infrastructure.adapter.in.web;

import com.simpleshop.order.application.command.CancelOrderCommand;
import com.simpleshop.order.application.command.ConfirmOrderCommand;
import com.simpleshop.order.application.command.DeliverOrderCommand;
import com.simpleshop.order.application.command.ShipOrderCommand;
import com.simpleshop.order.application.port.in.*;
import com.simpleshop.order.application.port.out.OrderRepository;
import com.simpleshop.order.application.query.GetOrderQuery;
import com.simpleshop.order.application.query.OrderView;
import com.simpleshop.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {
    
    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final ShipOrderUseCase shipOrderUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final OrderRepository orderRepository;
    
    public OrderAdminController(ConfirmOrderUseCase confirmOrderUseCase,
                                CancelOrderUseCase cancelOrderUseCase,
                                ShipOrderUseCase shipOrderUseCase,
                                DeliverOrderUseCase deliverOrderUseCase,
                                GetOrderUseCase getOrderUseCase,
                                OrderRepository orderRepository) {
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.shipOrderUseCase = shipOrderUseCase;
        this.deliverOrderUseCase = deliverOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.orderRepository = orderRepository;
    }
    
    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAll(pageable);
        model.addAttribute("orders", orders);
        return "admin/orders/list";
    }
    
    @GetMapping("/{orderId}")
    public String viewOrder(@PathVariable UUID orderId, Model model) {
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(orderId))
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        model.addAttribute("order", order);
        return "admin/orders/detail";
    }
    
    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable UUID orderId, RedirectAttributes redirectAttributes) {
        try {
            confirmOrderUseCase.execute(new ConfirmOrderCommand(orderId));
            redirectAttributes.addFlashAttribute("success", "Order confirmed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
    
    @PostMapping("/{orderId}/ship")
    public String shipOrder(@PathVariable UUID orderId, RedirectAttributes redirectAttributes) {
        try {
            shipOrderUseCase.execute(new ShipOrderCommand(orderId));
            redirectAttributes.addFlashAttribute("success", "Order marked as shipped");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
    
    @PostMapping("/{orderId}/deliver")
    public String deliverOrder(@PathVariable UUID orderId, RedirectAttributes redirectAttributes) {
        try {
            deliverOrderUseCase.execute(new DeliverOrderCommand(orderId));
            redirectAttributes.addFlashAttribute("success", "Order marked as delivered");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
    
    @PostMapping("/{orderId}/payment-success")
    public String simulatePaymentSuccess(@PathVariable UUID orderId, RedirectAttributes redirectAttributes) {
        try {
            confirmOrderUseCase.execute(new ConfirmOrderCommand(orderId));
            redirectAttributes.addFlashAttribute("success", "Payment successful! Order confirmed and stock deducted from inventory.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment simulation failed: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
    
    @PostMapping("/{orderId}/payment-failed")
    public String simulatePaymentFailed(@PathVariable UUID orderId, RedirectAttributes redirectAttributes) {
        try {
            cancelOrderUseCase.execute(new CancelOrderCommand(orderId, "Payment failed"));
            redirectAttributes.addFlashAttribute("success", "Payment failed! Order cancelled and reserved stock released.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment failure simulation failed: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }
}
