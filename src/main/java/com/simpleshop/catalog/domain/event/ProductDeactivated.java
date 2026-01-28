package com.simpleshop.catalog.domain.event;

import com.simpleshop.catalog.domain.model.vo.ProductId;
import com.simpleshop.shared.domain.model.DomainEvent;

public class ProductDeactivated extends DomainEvent {
    
    private final ProductId productId;
    
    public ProductDeactivated(ProductId productId) {
        super();
        this.productId = productId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
}
