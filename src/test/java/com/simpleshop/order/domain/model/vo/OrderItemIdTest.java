package com.simpleshop.order.domain.model.vo;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class OrderItemIdTest {

    @Test
    public void of_createsOrderItemId() {
        UUID uuid = UUID.randomUUID();
        OrderItemId id = OrderItemId.of(uuid);
        assertEquals(id.getValue(), uuid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForNull() {
        OrderItemId.of(null);
    }

    @Test
    public void generate_createsRandomId() {
        OrderItemId id1 = OrderItemId.generate();
        OrderItemId id2 = OrderItemId.generate();
        assertNotNull(id1.getValue());
        assertNotEquals(id1, id2);
    }

    @Test
    public void equals_returnsTrueForSameValue() {
        UUID uuid = UUID.randomUUID();
        OrderItemId id1 = OrderItemId.of(uuid);
        OrderItemId id2 = OrderItemId.of(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
