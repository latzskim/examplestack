package com.simpleshop.shared.domain.model.vo;

import com.simpleshop.shared.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public final class Address extends ValueObject {
    private String street;
    private String city;
    private String postalCode;
    private String country;
    
    protected Address() {}
    
    private Address(String street, String city, String postalCode, String country) {
        if (street == null || street.isBlank()) throw new IllegalArgumentException("Street cannot be empty");
        if (city == null || city.isBlank()) throw new IllegalArgumentException("City cannot be empty");
        if (postalCode == null || postalCode.isBlank()) throw new IllegalArgumentException("Postal code cannot be empty");
        if (country == null || country.isBlank()) throw new IllegalArgumentException("Country cannot be empty");
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
    }
    
    public static Address of(String street, String city, String postalCode, String country) {
        return new Address(street, city, postalCode, country);
    }
    
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) && Objects.equals(city, address.city) 
            && Objects.equals(postalCode, address.postalCode) && Objects.equals(country, address.country);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(street, city, postalCode, country);
    }
    
    @Override
    public String toString() {
        return street + ", " + city + " " + postalCode + ", " + country;
    }
}
