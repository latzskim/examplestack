package com.simpleshop.catalog.domain.model;

import com.simpleshop.catalog.domain.event.ProductCreated;
import com.simpleshop.catalog.domain.event.ProductDeactivated;
import com.simpleshop.catalog.domain.event.ProductUpdated;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.catalog.domain.model.vo.ProductId;
import com.simpleshop.catalog.domain.model.vo.Sku;
import com.simpleshop.shared.domain.model.AggregateRoot;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product extends AggregateRoot<Product> {
    
    @Id
    private UUID id;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "sku", nullable = false, unique = true, length = 50))
    private Sku sku;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false, length = 3))
    })
    private Money price;
    
    @Column(name = "category_id")
    private UUID categoryId;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "active", nullable = false)
    private boolean active;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    protected Product() {}
    
    private Product(ProductId id, String name, String description, Sku sku, Money price, UUID categoryId, String imageUrl) {
        this.id = id.getValue();
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.price = price;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public static Product create(String name, String description, Sku sku, Money price, UUID categoryId, String imageUrl) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name cannot be empty");
        if (sku == null) throw new IllegalArgumentException("SKU cannot be null");
        if (price == null) throw new IllegalArgumentException("Price cannot be null");
        
        Product product = new Product(ProductId.generate(), name, description, sku, price, categoryId, imageUrl);
        product.registerEvent(new ProductCreated(product.getProductId(), product.name, product.sku));
        return product;
    }

    public void update(String name, String description, Money price, UUID categoryId, String imageUrl) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name cannot be empty");
        if (price == null) throw new IllegalArgumentException("Price cannot be null");
        
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.updatedAt = Instant.now();
        registerEvent(new ProductUpdated(getProductId()));
    }
    
    public void deactivate() {
        if (!this.active) return;
        this.active = false;
        this.updatedAt = Instant.now();
        registerEvent(new ProductDeactivated(getProductId()));
    }
    
    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }
    
    public ProductId getProductId() {
        return ProductId.of(id);
    }
    
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Sku getSku() {
        return sku;
    }
    
    public Money getPrice() {
        return price;
    }
    
    public UUID getCategoryId() {
        return categoryId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
