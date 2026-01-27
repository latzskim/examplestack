package com.simpleshop.identity.domain.model.vo;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PersonNameTest {
    
    @Test
    public void of_createsPersonName() {
        PersonName name = PersonName.of("John", "Doe");
        
        assertEquals(name.getFirstName(), "John");
        assertEquals(name.getLastName(), "Doe");
    }
    
    @Test
    public void getFullName_combinesFirstAndLastName() {
        PersonName name = PersonName.of("John", "Doe");
        
        assertEquals(name.getFullName(), "John Doe");
    }
    
    @Test
    public void getFullName_handlesNullFirstName() {
        PersonName name = PersonName.of(null, "Doe");
        
        assertEquals(name.getFullName(), "Doe");
    }
    
    @Test
    public void getFullName_handlesNullLastName() {
        PersonName name = PersonName.of("John", null);
        
        assertEquals(name.getFullName(), "John");
    }
    
    @Test
    public void getFullName_handlesBothNull() {
        PersonName name = PersonName.of(null, null);
        
        assertEquals(name.getFullName(), "");
    }
    
    @Test
    public void of_trimsWhitespace() {
        PersonName name = PersonName.of("  John  ", "  Doe  ");
        
        assertEquals(name.getFirstName(), "John");
        assertEquals(name.getLastName(), "Doe");
    }
    
    @Test
    public void equals_returnsTrueForSameValues() {
        PersonName name1 = PersonName.of("John", "Doe");
        PersonName name2 = PersonName.of("John", "Doe");
        
        assertEquals(name1, name2);
        assertEquals(name1.hashCode(), name2.hashCode());
    }
}
