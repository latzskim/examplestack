package com.simpleshop.notification.domain.model;

import com.simpleshop.notification.domain.model.vo.NotificationStatus;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NotificationStatusTest {

    @Test
    public void isFinal_returnsFalseForPending() {
        assertFalse(NotificationStatus.PENDING.isFinal());
    }

    @Test
    public void isFinal_returnsTrueForSent() {
        assertTrue(NotificationStatus.SENT.isFinal());
    }

    @Test
    public void isFinal_returnsTrueForFailed() {
        assertTrue(NotificationStatus.FAILED.isFinal());
    }

    @Test
    public void values_returnsAllStatuses() {
        NotificationStatus[] statuses = NotificationStatus.values();
        
        assertEquals(statuses.length, 3);
        assertTrue(contains(statuses, NotificationStatus.PENDING));
        assertTrue(contains(statuses, NotificationStatus.SENT));
        assertTrue(contains(statuses, NotificationStatus.FAILED));
    }

    @Test
    public void valueOf_returnsCorrectStatus() {
        assertEquals(NotificationStatus.valueOf("PENDING"), NotificationStatus.PENDING);
        assertEquals(NotificationStatus.valueOf("SENT"), NotificationStatus.SENT);
        assertEquals(NotificationStatus.valueOf("FAILED"), NotificationStatus.FAILED);
    }

    private boolean contains(NotificationStatus[] statuses, NotificationStatus status) {
        for (NotificationStatus s : statuses) {
            if (s == status) return true;
        }
        return false;
    }
}
