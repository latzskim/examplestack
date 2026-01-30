package com.simpleshop.shipping.domain.model.vo;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class TrackingNumberTest {

    @Test
    public void of_createsValidTrackingNumber() {
        TrackingNumber tn = TrackingNumber.of("SHIP-20240130-00001");
        assertEquals(tn.getValue(), "SHIP-20240130-00001");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForNull() {
        TrackingNumber.of(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForBlank() {
        TrackingNumber.of("   ");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForTooLong() {
        TrackingNumber.of("SHIP-20240130-00001-VERY-LONG-TRACKING-NUMBER-THAT-EXCEEDS-LIMIT");
    }

    @Test
    public void equals_returnsTrueForSameValue() {
        TrackingNumber tn1 = TrackingNumber.of("SHIP-2024-00001");
        TrackingNumber tn2 = TrackingNumber.of("SHIP-2024-00001");
        assertEquals(tn1, tn2);
        assertEquals(tn1.hashCode(), tn2.hashCode());
    }

    @Test
    public void equals_returnsFalseForDifferentValue() {
        TrackingNumber tn1 = TrackingNumber.of("SHIP-2024-00001");
        TrackingNumber tn2 = TrackingNumber.of("SHIP-2024-00002");
        assertNotEquals(tn1, tn2);
    }

    @Test
    public void toString_returnsValue() {
        TrackingNumber tn = TrackingNumber.of("SHIP-2024-12345");
        assertEquals(tn.toString(), "SHIP-2024-12345");
    }
}
