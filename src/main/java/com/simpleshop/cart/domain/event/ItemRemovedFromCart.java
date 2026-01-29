package com.simpleshop.cart.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class ItemRemovedFromCart extends DomainEvent {
    private final UUID cartId;
    private final UUID productId;
    
    public ItemRemovedFromCart(UUID cartId, UUID productId) {
        super();
        this.cartId = cartId;
        this.productId = productId;
    }
    
    public UUID getCartId() {
        return cartId;
    }
    
    public UUID getProductId() {
        return productId;
    }
}
