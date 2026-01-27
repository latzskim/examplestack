package com.simpleshop.identity.domain.model.vo;

import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

public class UserIdTest {
    
    @Test
    public void of_createsUserIdFromUUID() {
        UUID uuid = UUID.randomUUID();
        UserId userId = UserId.of(uuid);
        
        assertEquals(userId.getValue(), uuid);
    }
    
    @Test
    public void generate_createsRandomUserId() {
        UserId userId1 = UserId.generate();
        UserId userId2 = UserId.generate();
        
        assertNotEquals(userId1, userId2);
    }
    
    @Test
    public void fromString_parsesUUIDString() {
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";
        UserId userId = UserId.fromString(uuidString);
        
        assertEquals(userId.getValue().toString(), uuidString);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsExceptionForNull() {
        UserId.of(null);
    }
    
    @Test
    public void equals_returnsTrueForSameValue() {
        UUID uuid = UUID.randomUUID();
        UserId userId1 = UserId.of(uuid);
        UserId userId2 = UserId.of(uuid);
        
        assertEquals(userId1, userId2);
        assertEquals(userId1.hashCode(), userId2.hashCode());
    }
}
