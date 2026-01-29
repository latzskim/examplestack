package com.simpleshop.cart.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class CartCleared extends DomainEvent {
    private final UUID cartId;
    private final int itemCount;
    
    public CartCleared(UUID cartId, int itemCount) {
        super();
        this.cartId = cartId;
        this.itemCount = itemCount;
    }
    
    public UUID getCartId() {
        return cartId;
    }
    
    public int getItemCount() {
        return itemCount;
    }
}
