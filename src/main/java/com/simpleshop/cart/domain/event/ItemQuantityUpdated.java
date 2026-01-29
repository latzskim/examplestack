package com.simpleshop.cart.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class ItemQuantityUpdated extends DomainEvent {
    private final UUID cartId;
    private final UUID productId;
    private final int oldQuantity;
    private final int newQuantity;
    
    public ItemQuantityUpdated(UUID cartId, UUID productId, int oldQuantity, int newQuantity) {
        super();
        this.cartId = cartId;
        this.productId = productId;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
    }
    
    public UUID getCartId() {
        return cartId;
    }
    
    public UUID getProductId() {
        return productId;
    }
    
    public int getOldQuantity() {
        return oldQuantity;
    }
    
    public int getNewQuantity() {
        return newQuantity;
    }
}
