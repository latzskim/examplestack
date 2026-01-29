package com.simpleshop.cart.domain.model;

import com.simpleshop.cart.domain.event.*;
import com.simpleshop.cart.domain.exception.CartItemNotFoundException;
import com.simpleshop.cart.domain.model.vo.SessionId;
import com.simpleshop.catalog.domain.model.vo.Money;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.testng.Assert.*;

public class CartTest {

    private SessionId sessionId;
    private UUID userId;
    private UUID productId;
    private Money price;

    @BeforeMethod
    public void setUp() {
        sessionId = SessionId.of("test-session-123");
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        price = Money.usd(new BigDecimal("29.99"));
    }

    @Test
    public void createForSession_createsAnonymousCart() {
        Cart cart = Cart.createForSession(sessionId);

        assertNotNull(cart.getId());
        assertEquals(cart.getSessionId(), sessionId);
        assertNull(cart.getUserId());
        assertTrue(cart.getItems().isEmpty());
        assertNotNull(cart.getCreatedAt());
        assertNotNull(cart.getUpdatedAt());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void createForSession_throwsForNullSessionId() {
        Cart.createForSession(null);
    }

    @Test
    public void createForUser_createsAuthenticatedCart() {
        Cart cart = Cart.createForUser(userId);

        assertNotNull(cart.getId());
        assertNull(cart.getSessionId());
        assertEquals(cart.getUserId(), userId);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void createForUser_throwsForNullUserId() {
        Cart.createForUser(null);
    }

    @Test
    public void addItem_addsNewItem() {
        Cart cart = Cart.createForSession(sessionId);

        cart.addItem(productId, price, 2);

        assertEquals(cart.getItems().size(), 1);
        CartItem item = cart.getItems().get(0);
        assertEquals(item.getProductId(), productId);
        assertEquals(item.getQuantity().getValue(), 2);
        assertEquals(item.getPriceAtAddition(), price);
    }

    @Test
    public void addItem_registersItemAddedEvent() {
        Cart cart = Cart.createForSession(sessionId);

        cart.addItem(productId, price, 2);

        var events = cart.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ItemAddedToCart);
        ItemAddedToCart event = (ItemAddedToCart) events.iterator().next();
        assertEquals(event.getCartId(), cart.getId());
        assertEquals(event.getProductId(), productId);
        assertEquals(event.getQuantity(), 2);
    }

    @Test
    public void addItem_withExistingProduct_updatesQuantity() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.clearEvents();

        cart.addItem(productId, price, 3);

        assertEquals(cart.getItems().size(), 1);
        assertEquals(cart.getItems().get(0).getQuantity().getValue(), 5);
    }

    @Test
    public void addItem_withExistingProduct_registersQuantityUpdatedEvent() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.clearEvents();

        cart.addItem(productId, price, 3);

        var events = cart.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ItemQuantityUpdated);
        ItemQuantityUpdated event = (ItemQuantityUpdated) events.iterator().next();
        assertEquals(event.getOldQuantity(), 2);
        assertEquals(event.getNewQuantity(), 5);
    }

    @Test
    public void removeItem_removesItem() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.clearEvents();

        cart.removeItem(productId);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    public void removeItem_registersItemRemovedEvent() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.clearEvents();

        cart.removeItem(productId);

        var events = cart.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ItemRemovedFromCart);
        ItemRemovedFromCart event = (ItemRemovedFromCart) events.iterator().next();
        assertEquals(event.getCartId(), cart.getId());
        assertEquals(event.getProductId(), productId);
    }

    @Test(expectedExceptions = CartItemNotFoundException.class)
    public void removeItem_throwsWhenItemNotFound() {
        Cart cart = Cart.createForSession(sessionId);

        cart.removeItem(productId);
    }

    @Test
    public void updateItemQuantity_updatesQuantity() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.clearEvents();

        cart.updateItemQuantity(productId, 5);

        assertEquals(cart.getItems().get(0).getQuantity().getValue(), 5);
    }

    @Test
    public void updateItemQuantity_registersQuantityUpdatedEvent() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.clearEvents();

        cart.updateItemQuantity(productId, 5);

        var events = cart.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ItemQuantityUpdated);
        ItemQuantityUpdated event = (ItemQuantityUpdated) events.iterator().next();
        assertEquals(event.getOldQuantity(), 2);
        assertEquals(event.getNewQuantity(), 5);
    }

    @Test
    public void updateItemQuantity_withZero_removesItem() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.clearEvents();

        cart.updateItemQuantity(productId, 0);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    public void updateItemQuantity_withZero_registersItemRemovedEvent() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.clearEvents();

        cart.updateItemQuantity(productId, 0);

        var events = cart.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof ItemRemovedFromCart);
    }

    @Test(expectedExceptions = CartItemNotFoundException.class)
    public void updateItemQuantity_throwsWhenItemNotFound() {
        Cart cart = Cart.createForSession(sessionId);

        cart.updateItemQuantity(productId, 5);
    }

    @Test
    public void clear_removesAllItems() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.addItem(UUID.randomUUID(), Money.usd(new BigDecimal("19.99")), 1);
        cart.clearEvents();

        cart.clear();

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    public void clear_registersCartClearedEvent() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.addItem(UUID.randomUUID(), Money.usd(new BigDecimal("19.99")), 1);
        cart.clearEvents();

        cart.clear();

        var events = cart.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof CartCleared);
        CartCleared event = (CartCleared) events.iterator().next();
        assertEquals(event.getCartId(), cart.getId());
        assertEquals(event.getItemCount(), 2);
    }

    @Test
    public void assignToUser_assignsUserId() {
        Cart cart = Cart.createForSession(sessionId);

        cart.assignToUser(userId);

        assertEquals(cart.getUserId(), userId);
        assertNull(cart.getSessionId());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void assignToUser_throwsForNullUserId() {
        Cart cart = Cart.createForSession(sessionId);

        cart.assignToUser(null);
    }

    @Test
    public void mergeFrom_mergesItemsFromAnotherCart() {
        Cart targetCart = Cart.createForUser(userId);
        Cart sourceCart = Cart.createForSession(sessionId);
        sourceCart.addItem(productId, price, 2);
        targetCart.clearEvents();

        targetCart.mergeFrom(sourceCart);

        assertEquals(targetCart.getItems().size(), 1);
        assertEquals(targetCart.getItems().get(0).getProductId(), productId);
        assertEquals(targetCart.getItems().get(0).getQuantity().getValue(), 2);
    }

    @Test
    public void mergeFrom_combinesQuantitiesForSameProduct() {
        Cart targetCart = Cart.createForUser(userId);
        targetCart.addItem(productId, price, 3);
        Cart sourceCart = Cart.createForSession(sessionId);
        sourceCart.addItem(productId, price, 2);
        targetCart.clearEvents();

        targetCart.mergeFrom(sourceCart);

        assertEquals(targetCart.getItems().size(), 1);
        assertEquals(targetCart.getItems().get(0).getQuantity().getValue(), 5);
    }

    @Test
    public void mergeFrom_registersCartMergedEvent() {
        Cart targetCart = Cart.createForUser(userId);
        Cart sourceCart = Cart.createForSession(sessionId);
        sourceCart.addItem(productId, price, 2);
        targetCart.clearEvents();

        targetCart.mergeFrom(sourceCart);

        var events = targetCart.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof CartMerged);
        CartMerged event = (CartMerged) events.iterator().next();
        assertEquals(event.getTargetCartId(), targetCart.getId());
        assertEquals(event.getSourceCartId(), sourceCart.getId());
        assertEquals(event.getItemsMerged(), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void mergeFrom_throwsForNullCart() {
        Cart cart = Cart.createForUser(userId);

        cart.mergeFrom(null);
    }

    @Test
    public void getTotal_calculatesCorrectly() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, Money.usd(new BigDecimal("10.00")), 2);
        cart.addItem(UUID.randomUUID(), Money.usd(new BigDecimal("5.00")), 3);

        Money total = cart.getTotal();

        assertEquals(total, Money.usd(new BigDecimal("35.00")));
    }

    @Test
    public void getTotal_returnsZeroForEmptyCart() {
        Cart cart = Cart.createForSession(sessionId);

        Money total = cart.getTotal();

        assertEquals(total.getAmount(), BigDecimal.ZERO);
    }

    @Test
    public void getItemCount_returnsTotalQuantity() {
        Cart cart = Cart.createForSession(sessionId);
        cart.addItem(productId, price, 2);
        cart.addItem(UUID.randomUUID(), Money.usd(new BigDecimal("19.99")), 3);

        assertEquals(cart.getItemCount(), 5);
    }
}
