package com.simpleshop.order.domain.model.vo;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class OrderIdTest {

    @Test
    public void of_createsOrderId() {
        UUID uuid = UUID.randomUUID();
        OrderId orderId = OrderId.of(uuid);
        assertEquals(orderId.getValue(), uuid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForNull() {
        OrderId.of(null);
    }

    @Test
    public void generate_createsRandomOrderId() {
        OrderId orderId1 = OrderId.generate();
        OrderId orderId2 = OrderId.generate();
        assertNotNull(orderId1.getValue());
        assertNotEquals(orderId1, orderId2);
    }

    @Test
    public void equals_returnsTrueForSameValue() {
        UUID uuid = UUID.randomUUID();
        OrderId orderId1 = OrderId.of(uuid);
        OrderId orderId2 = OrderId.of(uuid);
        assertEquals(orderId1, orderId2);
        assertEquals(orderId1.hashCode(), orderId2.hashCode());
    }

    @Test
    public void equals_returnsFalseForDifferentValues() {
        OrderId orderId1 = OrderId.generate();
        OrderId orderId2 = OrderId.generate();
        assertNotEquals(orderId1, orderId2);
    }

    @Test
    public void toString_returnsUuidString() {
        UUID uuid = UUID.randomUUID();
        OrderId orderId = OrderId.of(uuid);
        assertEquals(orderId.toString(), uuid.toString());
    }
}
