package com.simpleshop.order.domain.model;

import com.simpleshop.shared.domain.model.vo.Money;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.testng.Assert.*;

public class OrderItemTest {

    @Test
    public void create_createsOrderItemWithCorrectValues() {
        UUID productId = UUID.randomUUID();
        String productName = "Test Product";
        int quantity = 3;
        Money unitPrice = Money.usd(new BigDecimal("25.00"));
        UUID warehouseId = UUID.randomUUID();

        OrderItem item = OrderItem.create(productId, productName, quantity, unitPrice, warehouseId);

        assertNotNull(item.getId());
        assertEquals(item.getProductId(), productId);
        assertEquals(item.getProductName(), productName);
        assertEquals(item.getQuantity().getValue(), quantity);
        assertEquals(item.getUnitPrice(), unitPrice);
        assertEquals(item.getWarehouseId(), warehouseId);
    }

    @Test
    public void create_allowsNullWarehouseId() {
        UUID productId = UUID.randomUUID();
        Money unitPrice = Money.usd(new BigDecimal("10.00"));

        OrderItem item = OrderItem.create(productId, "Product", 1, unitPrice, null);

        assertNull(item.getWarehouseId());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForNullProductId() {
        OrderItem.create(null, "Product", 1, Money.usd(new BigDecimal("10.00")), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForNullProductName() {
        OrderItem.create(UUID.randomUUID(), null, 1, Money.usd(new BigDecimal("10.00")), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForBlankProductName() {
        OrderItem.create(UUID.randomUUID(), "  ", 1, Money.usd(new BigDecimal("10.00")), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForZeroQuantity() {
        OrderItem.create(UUID.randomUUID(), "Product", 0, Money.usd(new BigDecimal("10.00")), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForNegativeQuantity() {
        OrderItem.create(UUID.randomUUID(), "Product", -1, Money.usd(new BigDecimal("10.00")), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void create_throwsForNullUnitPrice() {
        OrderItem.create(UUID.randomUUID(), "Product", 1, null, null);
    }

    @Test
    public void getSubtotal_calculatesCorrectly() {
        Money unitPrice = Money.usd(new BigDecimal("15.50"));
        OrderItem item = OrderItem.create(UUID.randomUUID(), "Product", 4, unitPrice, null);

        Money subtotal = item.getSubtotal();

        assertEquals(subtotal, Money.usd(new BigDecimal("62.00")));
    }

    @Test
    public void getSubtotal_returnsUnitPriceForQuantityOne() {
        Money unitPrice = Money.usd(new BigDecimal("99.99"));
        OrderItem item = OrderItem.create(UUID.randomUUID(), "Product", 1, unitPrice, null);

        Money subtotal = item.getSubtotal();

        assertEquals(subtotal, unitPrice);
    }

    @Test
    public void getOrderItemId_returnsWrappedId() {
        OrderItem item = OrderItem.create(
            UUID.randomUUID(), 
            "Product", 
            1, 
            Money.usd(new BigDecimal("10.00")), 
            null
        );

        assertNotNull(item.getOrderItemId());
        assertEquals(item.getOrderItemId().getValue(), item.getId());
    }
}
