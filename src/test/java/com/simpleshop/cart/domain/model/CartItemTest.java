package com.simpleshop.cart.domain.model;

import com.simpleshop.catalog.domain.model.vo.Money;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.testng.Assert.*;

public class CartItemTest {

    @Test
    public void create_withValidParams_createsCartItem() {
        UUID productId = UUID.randomUUID();
        Money price = Money.usd(new BigDecimal("29.99"));

        CartItem item = CartItem.create(productId, price, 2);

        assertNotNull(item.getId());
        assertEquals(item.getProductId(), productId);
        assertEquals(item.getPriceAtAddition(), price);
        assertEquals(item.getQuantity().getValue(), 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_withNullProductId_throws() {
        CartItem.create(null, Money.usd(BigDecimal.TEN), 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_withNullPrice_throws() {
        CartItem.create(UUID.randomUUID(), null, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_withZeroQuantity_throws() {
        CartItem.create(UUID.randomUUID(), Money.usd(BigDecimal.TEN), 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_withNegativeQuantity_throws() {
        CartItem.create(UUID.randomUUID(), Money.usd(BigDecimal.TEN), -1);
    }

    @Test
    public void updateQuantity_updatesQuantity() {
        CartItem item = CartItem.create(UUID.randomUUID(), Money.usd(BigDecimal.TEN), 1);

        item.updateQuantity(5);

        assertEquals(item.getQuantity().getValue(), 5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateQuantity_withZero_throws() {
        CartItem item = CartItem.create(UUID.randomUUID(), Money.usd(BigDecimal.TEN), 1);

        item.updateQuantity(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void updateQuantity_withNegative_throws() {
        CartItem item = CartItem.create(UUID.randomUUID(), Money.usd(BigDecimal.TEN), 1);

        item.updateQuantity(-1);
    }

    @Test
    public void getSubtotal_calculatesCorrectly() {
        CartItem item = CartItem.create(UUID.randomUUID(), Money.usd(new BigDecimal("10.00")), 3);

        Money subtotal = item.getSubtotal();

        assertEquals(subtotal, Money.usd(new BigDecimal("30.00")));
    }

    @Test
    public void getCartItemId_returnsCartItemId() {
        CartItem item = CartItem.create(UUID.randomUUID(), Money.usd(BigDecimal.TEN), 1);

        assertNotNull(item.getCartItemId());
        assertEquals(item.getCartItemId().getValue(), item.getId());
    }
}
