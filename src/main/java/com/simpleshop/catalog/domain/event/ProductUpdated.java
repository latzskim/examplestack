package com.simpleshop.catalog.domain.event;

import com.simpleshop.catalog.domain.model.vo.ProductId;
import com.simpleshop.shared.domain.model.DomainEvent;

public class ProductUpdated extends DomainEvent {
    
    private final ProductId productId;
    
    public ProductUpdated(ProductId productId) {
        super();
        this.productId = productId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
}
