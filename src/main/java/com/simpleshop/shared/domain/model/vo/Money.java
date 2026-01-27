package com.simpleshop.shared.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

@Embeddable
public final class Money extends ValueObject {
    private BigDecimal amount;
    private String currencyCode;
    
    protected Money() {}
    
    private Money(BigDecimal amount, Currency currency) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        if (currency == null) throw new IllegalArgumentException("Currency cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount cannot be negative");
        this.amount = amount.setScale(4, RoundingMode.HALF_UP);
        this.currencyCode = currency.getCurrencyCode();
    }
    
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }
    
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }
    
    public static Money usd(BigDecimal amount) {
        return of(amount, Currency.getInstance("USD"));
    }
    
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
    
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), getCurrency());
    }
    
    public Money subtract(Money other) {
        assertSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result cannot be negative");
        }
        return new Money(result, getCurrency());
    }
    
    public Money multiply(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), getCurrency());
    }
    
    private void assertSameCurrency(Money other) {
        if (!this.currencyCode.equals(other.currencyCode)) {
            throw new IllegalArgumentException("Cannot operate on different currencies: " + currencyCode + " and " + other.currencyCode);
        }
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public Currency getCurrency() {
        return Currency.getInstance(currencyCode);
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.compareTo(money.amount) == 0 && Objects.equals(currencyCode, money.currencyCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }
    
    @Override
    public String toString() {
        return currencyCode + " " + amount;
    }
}
