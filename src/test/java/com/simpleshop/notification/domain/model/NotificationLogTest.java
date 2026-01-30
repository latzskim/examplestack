package com.simpleshop.notification.domain.model;

import com.simpleshop.notification.domain.model.vo.NotificationId;
import com.simpleshop.notification.domain.model.vo.NotificationStatus;
import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.shared.domain.model.vo.Email;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NotificationLogTest {

    @Test
    public void createPending_createsNotificationWithPendingStatus() {
        Email recipient = Email.of("test@example.com");
        String subject = "Test Subject";
        
        NotificationLog log = NotificationLog.createPending(NotificationType.ORDER_CONFIRMATION, recipient, subject);
        
        assertNotNull(log.getId());
        assertEquals(log.getType(), NotificationType.ORDER_CONFIRMATION);
        assertEquals(log.getRecipientEmail(), recipient);
        assertEquals(log.getSubject(), subject);
        assertEquals(log.getStatus(), NotificationStatus.PENDING);
        assertNull(log.getSentAt());
        assertNull(log.getErrorMessage());
        assertNotNull(log.getCreatedAt());
    }

    @Test
    public void markAsSent_changesStatusToSent() {
        NotificationLog log = NotificationLog.createPending(
            NotificationType.SHIPMENT_UPDATE, 
            Email.of("test@example.com"), 
            "Test Subject"
        );
        
        log.markAsSent();
        
        assertEquals(log.getStatus(), NotificationStatus.SENT);
        assertNotNull(log.getSentAt());
        assertNull(log.getErrorMessage());
    }

    @Test
    public void markAsFailed_changesStatusToFailed() {
        NotificationLog log = NotificationLog.createPending(
            NotificationType.INVOICE,
            Email.of("test@example.com"),
            "Test Subject"
        );
        String errorMessage = "SMTP connection failed";
        
        log.markAsFailed(errorMessage);
        
        assertEquals(log.getStatus(), NotificationStatus.FAILED);
        assertEquals(log.getErrorMessage(), errorMessage);
        assertNull(log.getSentAt());
    }

    @Test
    public void isPending_returnsTrueWhenPending() {
        NotificationLog log = NotificationLog.createPending(
            NotificationType.USER_WELCOME,
            Email.of("test@example.com"),
            "Test Subject"
        );
        
        assertTrue(log.isPending());
    }

    @Test
    public void isPending_returnsFalseWhenSent() {
        NotificationLog log = NotificationLog.createPending(
            NotificationType.ORDER_CONFIRMATION,
            Email.of("test@example.com"),
            "Test Subject"
        );
        log.markAsSent();
        
        assertFalse(log.isPending());
    }

    @Test
    public void isPending_returnsFalseWhenFailed() {
        NotificationLog log = NotificationLog.createPending(
            NotificationType.SHIPMENT_UPDATE,
            Email.of("test@example.com"),
            "Test Subject"
        );
        log.markAsFailed("Error");
        
        assertFalse(log.isPending());
    }

    @Test
    public void createPending_withDifferentNotificationTypes() {
        Email recipient = Email.of("test@example.com");
        
        NotificationLog orderConf = NotificationLog.createPending(
            NotificationType.ORDER_CONFIRMATION, recipient, "Order Confirmation"
        );
        NotificationLog shipmentUpdate = NotificationLog.createPending(
            NotificationType.SHIPMENT_UPDATE, recipient, "Shipment Update"
        );
        NotificationLog shipmentCreated = NotificationLog.createPending(
            NotificationType.SHIPMENT_CREATED, recipient, "Shipment Created"
        );
        NotificationLog invoice = NotificationLog.createPending(
            NotificationType.INVOICE, recipient, "Invoice"
        );
        NotificationLog welcome = NotificationLog.createPending(
            NotificationType.USER_WELCOME, recipient, "Welcome"
        );
        
        assertEquals(orderConf.getType(), NotificationType.ORDER_CONFIRMATION);
        assertEquals(shipmentUpdate.getType(), NotificationType.SHIPMENT_UPDATE);
        assertEquals(shipmentCreated.getType(), NotificationType.SHIPMENT_CREATED);
        assertEquals(invoice.getType(), NotificationType.INVOICE);
        assertEquals(welcome.getType(), NotificationType.USER_WELCOME);
    }
}
