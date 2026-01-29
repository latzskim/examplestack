package com.simpleshop.cart.domain.model;

import com.simpleshop.cart.domain.model.vo.CartItemId;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.shared.domain.model.vo.Quantity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
public class CartItem {
    
    @Id
    private UUID id;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    private Quantity quantity;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false, length = 3))
    })
    private Money priceAtAddition;
    
    protected CartItem() {}
    
    private CartItem(CartItemId id, UUID productId, Quantity quantity, Money priceAtAddition) {
        this.id = id.getValue();
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtAddition = priceAtAddition;
    }
    
    public static CartItem create(UUID productId, Money priceAtAddition, int quantity) {
        if (productId == null) throw new IllegalArgumentException("Product ID cannot be null");
        if (priceAtAddition == null) throw new IllegalArgumentException("Price cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        
        return new CartItem(CartItemId.generate(), productId, Quantity.of(quantity), priceAtAddition);
    }
    
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity = Quantity.of(newQuantity);
    }
    
    public Money getSubtotal() {
        return priceAtAddition.multiply(quantity.getValue());
    }
    
    public CartItemId getCartItemId() {
        return CartItemId.of(id);
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getProductId() {
        return productId;
    }
    
    public Quantity getQuantity() {
        return quantity;
    }
    
    public Money getPriceAtAddition() {
        return priceAtAddition;
    }
}
