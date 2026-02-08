package com.simpleshop.cart.application.service;

import com.simpleshop.cart.application.command.*;
import com.simpleshop.cart.application.port.in.*;
import com.simpleshop.cart.application.port.out.CartRepository;
import com.simpleshop.cart.application.query.*;
import com.simpleshop.cart.domain.model.Cart;
import com.simpleshop.cart.domain.model.CartItem;
import com.simpleshop.cart.domain.model.vo.SessionId;
import com.simpleshop.catalog.application.port.out.ProductRepository;
import com.simpleshop.catalog.domain.model.Product;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.catalog.domain.model.vo.ProductId;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService implements AddItemToCartUseCase, RemoveItemFromCartUseCase,
        UpdateItemQuantityUseCase, GetCartUseCase, ClearCartUseCase, MergeCartUseCase {
    
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    
    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }
    
    @Override
    @WithSpan("cart.addItem")
    public CartView execute(AddItemToCartCommand command) {
        Cart cart = getOrCreateCart(command.sessionId(), command.userId());
        
        Product product = productRepository.findById(ProductId.of(command.productId()))
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + command.productId()));
        
        cart.addItem(command.productId(), product.getPrice(), command.quantity());
        cart = cartRepository.save(cart);
        
        return toCartView(cart);
    }
    
    @Override
    @WithSpan("cart.removeItem")
    public CartView execute(RemoveItemFromCartCommand command) {
        Cart cart = findCart(command.sessionId(), command.userId())
            .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        
        cart.removeItem(command.productId());
        cart = cartRepository.save(cart);
        
        return toCartView(cart);
    }
    
    @Override
    @WithSpan("cart.updateItemQuantity")
    public CartView execute(UpdateItemQuantityCommand command) {
        Cart cart = findCart(command.sessionId(), command.userId())
            .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        
        cart.updateItemQuantity(command.productId(), command.quantity());
        cart = cartRepository.save(cart);
        
        return toCartView(cart);
    }
    
    @Override
    @WithSpan("cart.getCart")
    public CartView execute(GetCartQuery query) {
        Cart cart = getOrCreateCart(query.sessionId(), query.userId());
        return toCartView(cart);
    }
    
    @Override
    @WithSpan("cart.clearCart")
    public void execute(ClearCartCommand command) {
        Optional<Cart> cartOpt = findCart(command.sessionId(), command.userId());
        
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.clear();
            cartRepository.save(cart);
        }
    }
    
    @Override
    @WithSpan("cart.mergeCart")
    public CartView execute(MergeCartCommand command) {
        Optional<Cart> sessionCartOpt = cartRepository.findBySessionId(SessionId.of(command.sessionId()));
        
        if (sessionCartOpt.isEmpty()) {
            return toCartView(getOrCreateCart(null, command.userId()));
        }
        
        Cart sessionCart = sessionCartOpt.get();
        Cart userCart = getOrCreateCart(null, command.userId());
        
        userCart.mergeFrom(sessionCart);
        userCart = cartRepository.save(userCart);
        
        cartRepository.deleteById(sessionCart.getCartId());
        
        return toCartView(userCart);
    }
    
    private Cart getOrCreateCart(String sessionId, UUID userId) {
        Optional<Cart> cartOpt = findCart(sessionId, userId);
        
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        
        Cart newCart;
        if (userId != null) {
            newCart = Cart.createForUser(userId);
        } else {
            newCart = Cart.createForSession(SessionId.of(sessionId));
        }
        
        return cartRepository.save(newCart);
    }
    
    private Optional<Cart> findCart(String sessionId, UUID userId) {
        if (userId != null) {
            return cartRepository.findByUserId(userId);
        }
        if (sessionId != null) {
            return cartRepository.findBySessionId(SessionId.of(sessionId));
        }
        return Optional.empty();
    }
    
    private CartView toCartView(Cart cart) {
        List<UUID> productIds = cart.getItems().stream()
            .map(CartItem::getProductId)
            .distinct()
            .toList();
        Map<UUID, String> productNames = productRepository.findByIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Product::getName, (left, right) -> left));

        List<CartItemView> itemViews = new ArrayList<>();
        
        for (CartItem item : cart.getItems()) {
            String productName = productNames.getOrDefault(item.getProductId(), "Unknown Product");
            
            Money price = item.getPriceAtAddition();
            Money subtotal = item.getSubtotal();
            
            itemViews.add(new CartItemView(
                item.getProductId(),
                productName,
                price.getAmount(),
                price.getCurrency(),
                item.getQuantity().getValue(),
                subtotal.getAmount()
            ));
        }
        
        Money total = cart.getTotal();
        
        return new CartView(
            cart.getId(),
            itemViews,
            total.getAmount(),
            total.getCurrency(),
            cart.getItemCount()
        );
    }
}
