package com.simpleshop.catalog.domain.model.vo;

import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.*;

public class MoneyTest {

    @Test
    public void of_createsMoneyWithAmountAndCurrency() {
        Money money = Money.of(new BigDecimal("29.99"), "usd");
        
        assertEquals(money.getAmount(), new BigDecimal("29.99"));
        assertEquals(money.getCurrency(), "USD");
    }
    
    @Test
    public void usd_createsMoneyWithUsdCurrency() {
        Money money = Money.usd(new BigDecimal("19.99"));
        
        assertEquals(money.getAmount(), new BigDecimal("19.99"));
        assertEquals(money.getCurrency(), "USD");
    }
    
    @Test
    public void of_allowsZeroAmount() {
        Money money = Money.usd(BigDecimal.ZERO);
        
        assertEquals(money.getAmount(), BigDecimal.ZERO);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForNullAmount() {
        Money.of(null, "USD");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForNegativeAmount() {
        Money.of(new BigDecimal("-10"), "USD");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForNullCurrency() {
        Money.of(BigDecimal.TEN, null);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForInvalidCurrencyLength() {
        Money.of(BigDecimal.TEN, "US");
    }
    
    @Test
    public void add_addsTwoMoneyWithSameCurrency() {
        Money m1 = Money.usd(new BigDecimal("10.00"));
        Money m2 = Money.usd(new BigDecimal("5.50"));
        
        Money result = m1.add(m2);
        
        assertEquals(result.getAmount(), new BigDecimal("15.50"));
        assertEquals(result.getCurrency(), "USD");
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void add_throwsExceptionForDifferentCurrencies() {
        Money m1 = Money.usd(BigDecimal.TEN);
        Money m2 = Money.of(BigDecimal.TEN, "EUR");
        
        m1.add(m2);
    }
    
    @Test
    public void multiply_multipliesAmountByQuantity() {
        Money money = Money.usd(new BigDecimal("9.99"));
        
        Money result = money.multiply(3);
        
        assertEquals(result.getAmount(), new BigDecimal("29.97"));
        assertEquals(result.getCurrency(), "USD");
    }
    
    @Test
    public void equals_returnsTrueForSameAmountAndCurrency() {
        Money m1 = Money.usd(new BigDecimal("19.99"));
        Money m2 = Money.usd(new BigDecimal("19.99"));
        
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }
    
    @Test
    public void equals_returnsFalseForDifferentAmount() {
        Money m1 = Money.usd(new BigDecimal("19.99"));
        Money m2 = Money.usd(new BigDecimal("29.99"));
        
        assertNotEquals(m1, m2);
    }
    
    @Test
    public void equals_returnsFalseForDifferentCurrency() {
        Money m1 = Money.usd(BigDecimal.TEN);
        Money m2 = Money.of(BigDecimal.TEN, "EUR");
        
        assertNotEquals(m1, m2);
    }
    
    @Test
    public void toString_returnsFormattedString() {
        Money money = Money.usd(new BigDecimal("29.99"));
        
        assertEquals(money.toString(), "29.99 USD");
    }
}
