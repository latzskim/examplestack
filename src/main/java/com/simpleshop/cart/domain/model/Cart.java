package com.simpleshop.cart.domain.model;

import com.simpleshop.cart.domain.event.*;
import com.simpleshop.cart.domain.exception.CartItemNotFoundException;
import com.simpleshop.cart.domain.model.vo.CartId;
import com.simpleshop.cart.domain.model.vo.SessionId;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.shared.domain.model.AggregateRoot;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "carts")
public class Cart extends AggregateRoot<Cart> {
    
    @Id
    private UUID id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "session_id"))
    private SessionId sessionId;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private List<CartItem> items = new ArrayList<>();
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    protected Cart() {}
    
    private Cart(SessionId sessionId, UUID userId) {
        this.id = UUID.randomUUID();
        this.sessionId = sessionId;
        this.userId = userId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public static Cart createForSession(SessionId sessionId) {
        if (sessionId == null) throw new IllegalArgumentException("SessionId cannot be null");
        return new Cart(sessionId, null);
    }
    
    public static Cart createForUser(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("UserId cannot be null");
        return new Cart(null, userId);
    }
    
    public void addItem(UUID productId, Money price, int quantity) {
        Optional<CartItem> existingItem = findItem(productId);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int oldQuantity = item.getQuantity().getValue();
            int newQuantity = oldQuantity + quantity;
            item.updateQuantity(newQuantity);
            registerEvent(new ItemQuantityUpdated(this.id, productId, oldQuantity, newQuantity));
        } else {
            CartItem newItem = CartItem.create(productId, price, quantity);
            items.add(newItem);
            registerEvent(new ItemAddedToCart(this.id, productId, quantity));
        }
        this.updatedAt = Instant.now();
    }
    
    public void removeItem(UUID productId) {
        CartItem item = findItem(productId)
            .orElseThrow(() -> new CartItemNotFoundException(productId));
        items.remove(item);
        registerEvent(new ItemRemovedFromCart(this.id, productId));
        this.updatedAt = Instant.now();
    }
    
    public void updateItemQuantity(UUID productId, int quantity) {
        CartItem item = findItem(productId)
            .orElseThrow(() -> new CartItemNotFoundException(productId));
        int oldQuantity = item.getQuantity().getValue();
        if (quantity == 0) {
            items.remove(item);
            registerEvent(new ItemRemovedFromCart(this.id, productId));
        } else {
            item.updateQuantity(quantity);
            registerEvent(new ItemQuantityUpdated(this.id, productId, oldQuantity, quantity));
        }
        this.updatedAt = Instant.now();
    }
    
    public void clear() {
        int itemCount = items.size();
        items.clear();
        registerEvent(new CartCleared(this.id, itemCount));
        this.updatedAt = Instant.now();
    }
    
    public void assignToUser(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("UserId cannot be null");
        this.userId = userId;
        this.sessionId = null;
        this.updatedAt = Instant.now();
    }
    
    public void mergeFrom(Cart anonymousCart) {
        if (anonymousCart == null) throw new IllegalArgumentException("Source cart cannot be null");
        int itemsMerged = 0;
        for (CartItem sourceItem : anonymousCart.getItems()) {
            Optional<CartItem> existingItem = findItem(sourceItem.getProductId());
            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                int newQuantity = item.getQuantity().getValue() + sourceItem.getQuantity().getValue();
                item.updateQuantity(newQuantity);
            } else {
                CartItem newItem = CartItem.create(
                    sourceItem.getProductId(),
                    sourceItem.getPriceAtAddition(),
                    sourceItem.getQuantity().getValue()
                );
                items.add(newItem);
            }
            itemsMerged++;
        }
        registerEvent(new CartMerged(this.id, anonymousCart.getId(), itemsMerged));
        this.updatedAt = Instant.now();
    }
    
    public Money getTotal() {
        return items.stream()
            .map(CartItem::getSubtotal)
            .reduce(Money.usd(BigDecimal.ZERO), Money::add);
    }
    
    public Optional<CartItem> findItem(UUID productId) {
        return items.stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst();
    }
    
    public UUID getId() {
        return id;
    }
    
    public CartId getCartId() {
        return CartId.of(id);
    }
    
    public SessionId getSessionId() {
        return sessionId;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public int getItemCount() {
        return items.stream()
            .mapToInt(item -> item.getQuantity().getValue())
            .sum();
    }
}
