package com.simpleshop.order.infrastructure.adapter.in.web;

import com.simpleshop.cart.application.port.in.GetCartUseCase;
import com.simpleshop.cart.application.query.CartView;
import com.simpleshop.cart.application.query.GetCartQuery;
import com.simpleshop.identity.infrastructure.security.ShopUserDetails;
import com.simpleshop.order.application.command.CancelOrderCommand;
import com.simpleshop.order.application.command.PlaceOrderFromCartCommand;
import com.simpleshop.order.application.port.in.*;
import com.simpleshop.order.application.query.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class OrderController {
    
    private static final String CART_SESSION_ID = "CART_SESSION_ID";
    
    private final PlaceOrderFromCartUseCase placeOrderFromCartUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final ListUserOrdersUseCase listUserOrdersUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetCartUseCase getCartUseCase;
    
    public OrderController(PlaceOrderFromCartUseCase placeOrderFromCartUseCase,
                           GetOrderUseCase getOrderUseCase,
                           ListUserOrdersUseCase listUserOrdersUseCase,
                           CancelOrderUseCase cancelOrderUseCase,
                           GetCartUseCase getCartUseCase) {
        this.placeOrderFromCartUseCase = placeOrderFromCartUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.listUserOrdersUseCase = listUserOrdersUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getCartUseCase = getCartUseCase;
    }
    
    @GetMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String showCheckoutForm(HttpSession session,
                                   @AuthenticationPrincipal ShopUserDetails user,
                                   Model model) {
        CartView cart = getCartUseCase.execute(new GetCartQuery(null, user.getUserId().getValue()));
        
        if (cart.itemCount() == 0) {
            return "redirect:/cart";
        }
        
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutForm", new CheckoutForm());
        return "checkout/form";
    }
    
    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String processCheckout(@ModelAttribute CheckoutForm form,
                                  HttpSession session,
                                  @AuthenticationPrincipal ShopUserDetails user,
                                  RedirectAttributes redirectAttributes) {
        try {
            String sessionId = (String) session.getAttribute(CART_SESSION_ID);
            UUID userId = user.getUserId().getValue();
            
            PlaceOrderFromCartCommand command = new PlaceOrderFromCartCommand(
                userId,
                sessionId,
                form.getStreet(),
                form.getCity(),
                form.getPostalCode(),
                form.getCountry()
            );
            
            OrderView order = placeOrderFromCartUseCase.execute(command);
            
            redirectAttributes.addFlashAttribute("orderNumber", order.orderNumber());
            return "redirect:/checkout/confirmation/" + order.id();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }
    
    @GetMapping("/checkout/confirmation/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public String showConfirmation(@PathVariable UUID orderId,
                                   @AuthenticationPrincipal ShopUserDetails user,
                                   Model model) {
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(orderId))
            .filter(o -> o.userId().equals(user.getUserId().getValue()))
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        model.addAttribute("order", order);
        return "checkout/confirmation";
    }
    
    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public String listOrders(@RequestParam(defaultValue = "0") int page,
                             @AuthenticationPrincipal ShopUserDetails user,
                             Model model) {
        UUID userId = user.getUserId().getValue();
        Page<OrderSummaryView> orders = listUserOrdersUseCase.execute(
            new ListUserOrdersQuery(userId, page, 10)
        );
        
        model.addAttribute("orders", orders);
        return "orders/list";
    }
    
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public String viewOrder(@PathVariable UUID orderId,
                            @AuthenticationPrincipal ShopUserDetails user,
                            Model model) {
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(orderId))
            .filter(o -> o.userId().equals(user.getUserId().getValue()))
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        model.addAttribute("order", order);
        return "orders/detail";
    }
    
    @PostMapping("/orders/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public String cancelOrder(@PathVariable UUID orderId,
                              @RequestParam(required = false) String reason,
                              @AuthenticationPrincipal ShopUserDetails user,
                              RedirectAttributes redirectAttributes) {
        OrderView order = getOrderUseCase.execute(new GetOrderQuery(orderId))
            .filter(o -> o.userId().equals(user.getUserId().getValue()))
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        try {
            cancelOrderUseCase.execute(new CancelOrderCommand(orderId, reason != null ? reason : "Customer requested"));
            redirectAttributes.addFlashAttribute("success", "Order cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/orders/" + orderId;
    }
    
    public static class CheckoutForm {
        private String street;
        private String city;
        private String postalCode;
        private String country;
        
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }
}
