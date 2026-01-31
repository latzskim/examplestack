package com.simpleshop.cart.infrastructure.adapter.in.web;

import com.simpleshop.cart.application.command.AddItemToCartCommand;
import com.simpleshop.cart.application.command.ClearCartCommand;
import com.simpleshop.cart.application.command.RemoveItemFromCartCommand;
import com.simpleshop.cart.application.command.UpdateItemQuantityCommand;
import com.simpleshop.cart.application.port.in.AddItemToCartUseCase;
import com.simpleshop.cart.application.port.in.ClearCartUseCase;
import com.simpleshop.cart.application.port.in.GetCartUseCase;
import com.simpleshop.cart.application.port.in.RemoveItemFromCartUseCase;
import com.simpleshop.cart.application.port.in.UpdateItemQuantityUseCase;
import com.simpleshop.cart.application.query.CartView;
import com.simpleshop.cart.application.query.GetCartQuery;
import com.simpleshop.identity.infrastructure.security.ShopUserDetails;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    private static final String CART_SESSION_ID = "CART_SESSION_ID";
    
    private final AddItemToCartUseCase addItemToCartUseCase;
    private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
    private final UpdateItemQuantityUseCase updateItemQuantityUseCase;
    private final GetCartUseCase getCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    
    public CartController(AddItemToCartUseCase addItemToCartUseCase,
                          RemoveItemFromCartUseCase removeItemFromCartUseCase,
                          UpdateItemQuantityUseCase updateItemQuantityUseCase,
                          GetCartUseCase getCartUseCase,
                          ClearCartUseCase clearCartUseCase) {
        this.addItemToCartUseCase = addItemToCartUseCase;
        this.removeItemFromCartUseCase = removeItemFromCartUseCase;
        this.updateItemQuantityUseCase = updateItemQuantityUseCase;
        this.getCartUseCase = getCartUseCase;
        this.clearCartUseCase = clearCartUseCase;
    }
    
    @GetMapping
    @NewSpan
    public String viewCart(HttpSession session,
                           @AuthenticationPrincipal ShopUserDetails user,
                           Model model) {
        String sessionId = getSessionId(session);
        UUID userId = getUserId(user);
        
        GetCartQuery query = userId != null
                ? new GetCartQuery(null, userId)
                : new GetCartQuery(sessionId, null);
        
        CartView cart = getCartUseCase.execute(query);
        model.addAttribute("cart", cart);
        
        return "cart/view";
    }
    
    @PostMapping("/add")
    @NewSpan
    public String addItem(@RequestParam UUID productId,
                          @RequestParam int quantity,
                          HttpSession session,
                          @AuthenticationPrincipal ShopUserDetails user) {
        String sessionId = getSessionId(session);
        UUID userId = getUserId(user);
        
        AddItemToCartCommand command = userId != null
                ? new AddItemToCartCommand(null, userId, productId, quantity)
                : new AddItemToCartCommand(sessionId, null, productId, quantity);
        
        addItemToCartUseCase.execute(command);
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove/{productId}")
    @NewSpan
    public String removeItem(@PathVariable UUID productId,
                             HttpSession session,
                             @AuthenticationPrincipal ShopUserDetails user) {
        String sessionId = getSessionId(session);
        UUID userId = getUserId(user);
        
        RemoveItemFromCartCommand command = userId != null
                ? new RemoveItemFromCartCommand(null, userId, productId)
                : new RemoveItemFromCartCommand(sessionId, null, productId);
        
        removeItemFromCartUseCase.execute(command);
        
        return "redirect:/cart";
    }
    
    @PostMapping("/update")
    @NewSpan
    public String updateQuantity(@RequestParam UUID productId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 @AuthenticationPrincipal ShopUserDetails user) {
        String sessionId = getSessionId(session);
        UUID userId = getUserId(user);
        
        UpdateItemQuantityCommand command = userId != null
                ? new UpdateItemQuantityCommand(null, userId, productId, quantity)
                : new UpdateItemQuantityCommand(sessionId, null, productId, quantity);
        
        updateItemQuantityUseCase.execute(command);
        
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    @NewSpan
    public String clearCart(HttpSession session,
                            @AuthenticationPrincipal ShopUserDetails user) {
        String sessionId = getSessionId(session);
        UUID userId = getUserId(user);
        
        ClearCartCommand command = userId != null
                ? new ClearCartCommand(null, userId)
                : new ClearCartCommand(sessionId, null);
        
        clearCartUseCase.execute(command);
        
        return "redirect:/cart";
    }
    
    private String getSessionId(HttpSession session) {
        String sessionId = (String) session.getAttribute(CART_SESSION_ID);
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute(CART_SESSION_ID, sessionId);
        }
        return sessionId;
    }
    
    private UUID getUserId(ShopUserDetails user) {
        if (user == null) {
            return null;
        }
        return user.getUserId().getValue();
    }
}
