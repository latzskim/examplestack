package com.simpleshop.order.domain.model.vo;

import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.*;

public class OrderNumberTest {

    @Test
    public void of_createsValidOrderNumber() {
        OrderNumber orderNumber = OrderNumber.of("ORD-2024-00001");
        assertEquals(orderNumber.getValue(), "ORD-2024-00001");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForNull() {
        OrderNumber.of(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForBlank() {
        OrderNumber.of("   ");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForInvalidFormat() {
        OrderNumber.of("INVALID-123");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForWrongYearDigits() {
        OrderNumber.of("ORD-24-00001");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForWrongSequenceDigits() {
        OrderNumber.of("ORD-2024-001");
    }

    @Test
    public void generate_createsValidOrderNumber() {
        OrderNumber orderNumber = OrderNumber.generate();
        assertNotNull(orderNumber.getValue());
        assertTrue(orderNumber.getValue().startsWith("ORD-" + LocalDate.now().getYear() + "-"));
    }

    @Test
    public void generate_createsUniqueNumbers() {
        OrderNumber num1 = OrderNumber.generate();
        OrderNumber num2 = OrderNumber.generate();
        assertNotEquals(num1, num2);
    }

    @Test
    public void equals_returnsTrueForSameValue() {
        OrderNumber num1 = OrderNumber.of("ORD-2024-00001");
        OrderNumber num2 = OrderNumber.of("ORD-2024-00001");
        assertEquals(num1, num2);
        assertEquals(num1.hashCode(), num2.hashCode());
    }

    @Test
    public void toString_returnsValue() {
        OrderNumber orderNumber = OrderNumber.of("ORD-2024-12345");
        assertEquals(orderNumber.toString(), "ORD-2024-12345");
    }
}
