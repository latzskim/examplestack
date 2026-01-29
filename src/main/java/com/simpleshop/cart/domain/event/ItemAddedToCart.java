package com.simpleshop.cart.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class ItemAddedToCart extends DomainEvent {
    private final UUID cartId;
    private final UUID productId;
    private final int quantity;
    
    public ItemAddedToCart(UUID cartId, UUID productId, int quantity) {
        super();
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public UUID getCartId() {
        return cartId;
    }
    
    public UUID getProductId() {
        return productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
}
