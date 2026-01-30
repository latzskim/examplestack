package com.simpleshop.notification.domain.model;

import com.simpleshop.notification.domain.model.vo.NotificationId;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NotificationIdTest {

    @Test
    public void generate_createsUniqueId() {
        NotificationId id1 = NotificationId.generate();
        NotificationId id2 = NotificationId.generate();
        
        assertNotNull(id1.getValue());
        assertNotNull(id2.getValue());
        assertNotEquals(id1.getValue(), id2.getValue());
    }

    @Test
    public void of_createsIdWithValue() {
        java.util.UUID uuid = java.util.UUID.randomUUID();
        NotificationId id = NotificationId.of(uuid);
        
        assertEquals(id.getValue(), uuid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void of_throwsForNullValue() {
        NotificationId.of(null);
    }

    @Test
    public void equals_returnsTrueForSameValue() {
        java.util.UUID uuid = java.util.UUID.randomUUID();
        NotificationId id1 = NotificationId.of(uuid);
        NotificationId id2 = NotificationId.of(uuid);
        
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void equals_returnsFalseForDifferentValue() {
        NotificationId id1 = NotificationId.generate();
        NotificationId id2 = NotificationId.generate();
        
        assertNotEquals(id1, id2);
    }

    @Test
    public void toString_returnsValueToString() {
        NotificationId id = NotificationId.generate();
        
        assertEquals(id.toString(), id.getValue().toString());
    }
}
