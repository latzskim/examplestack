package com.simpleshop.cart.domain.event;

import com.simpleshop.shared.domain.model.DomainEvent;
import java.util.UUID;

public class CartMerged extends DomainEvent {
    private final UUID targetCartId;
    private final UUID sourceCartId;
    private final int itemsMerged;
    
    public CartMerged(UUID targetCartId, UUID sourceCartId, int itemsMerged) {
        super();
        this.targetCartId = targetCartId;
        this.sourceCartId = sourceCartId;
        this.itemsMerged = itemsMerged;
    }
    
    public UUID getTargetCartId() {
        return targetCartId;
    }
    
    public UUID getSourceCartId() {
        return sourceCartId;
    }
    
    public int getItemsMerged() {
        return itemsMerged;
    }
}
