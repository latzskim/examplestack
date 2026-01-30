package com.simpleshop.shipping.domain.model.vo;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class ShipmentIdTest {

    @Test
    public void of_createsValidShipmentId() {
        UUID uuid = UUID.randomUUID();
        ShipmentId id = ShipmentId.of(uuid);
        assertEquals(id.getValue(), uuid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForNull() {
        ShipmentId.of(null);
    }

    @Test
    public void generate_createsUniqueId() {
        ShipmentId id1 = ShipmentId.generate();
        ShipmentId id2 = ShipmentId.generate();

        assertNotNull(id1.getValue());
        assertNotNull(id2.getValue());
        assertNotEquals(id1.getValue(), id2.getValue());
    }

    @Test
    public void equals_returnsTrueForSameValue() {
        UUID uuid = UUID.randomUUID();
        ShipmentId id1 = ShipmentId.of(uuid);
        ShipmentId id2 = ShipmentId.of(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void equals_returnsFalseForDifferentValue() {
        ShipmentId id1 = ShipmentId.generate();
        ShipmentId id2 = ShipmentId.generate();
        assertNotEquals(id1, id2);
    }

    @Test
    public void toString_returnsValueAsString() {
        UUID uuid = UUID.randomUUID();
        ShipmentId id = ShipmentId.of(uuid);
        assertEquals(id.toString(), uuid.toString());
    }
}
