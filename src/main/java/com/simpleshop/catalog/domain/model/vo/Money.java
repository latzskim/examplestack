package com.simpleshop.catalog.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public final class Money extends ValueObject {
    
    private BigDecimal amount;
    private String currency;
    
    protected Money() {}
    
    private Money(BigDecimal amount, String currency) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount cannot be negative");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency cannot be empty");
        if (currency.length() != 3) throw new IllegalArgumentException("Currency must be a 3-letter code");
        this.amount = amount;
        this.currency = currency.toUpperCase();
    }
    
    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }
    
    public static Money usd(BigDecimal amount) {
        return new Money(amount, "USD");
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
    
    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
