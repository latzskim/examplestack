package com.simpleshop.catalog.domain.event;

import com.simpleshop.catalog.domain.model.vo.ProductId;
import com.simpleshop.catalog.domain.model.vo.Sku;
import com.simpleshop.shared.domain.model.DomainEvent;

public class ProductCreated extends DomainEvent {
    
    private final ProductId productId;
    private final String name;
    private final Sku sku;
    
    public ProductCreated(ProductId productId, String name, Sku sku) {
        super();
        this.productId = productId;
        this.name = name;
        this.sku = sku;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public String getName() {
        return name;
    }
    
    public Sku getSku() {
        return sku;
    }
}
