package com.simpleshop.notification.domain.event;

import com.simpleshop.notification.domain.model.vo.NotificationId;
import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.shared.domain.model.DomainEvent;
import com.simpleshop.shared.domain.model.vo.Email;
import java.time.Instant;
import java.util.UUID;

public final class NotificationSent extends DomainEvent {
    private final NotificationId notificationId;
    private final NotificationType type;
    private final Email recipientEmail;
    private final String subject;
    
    private NotificationSent(NotificationId notificationId, NotificationType type, Email recipientEmail, String subject) {
        super();
        this.notificationId = notificationId;
        this.type = type;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
    }
    
    public static NotificationSent create(NotificationId notificationId, NotificationType type, Email recipientEmail, String subject) {
        return new NotificationSent(notificationId, type, recipientEmail, subject);
    }
    
    public NotificationId getNotificationId() {
        return notificationId;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public Email getRecipientEmail() {
        return recipientEmail;
    }
    
    public String getSubject() {
        return subject;
    }
}
