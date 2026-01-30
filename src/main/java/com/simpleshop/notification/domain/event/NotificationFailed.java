package com.simpleshop.notification.domain.event;

import com.simpleshop.notification.domain.model.vo.NotificationId;
import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.shared.domain.model.DomainEvent;
import com.simpleshop.shared.domain.model.vo.Email;
import java.time.Instant;
import java.util.UUID;

public final class NotificationFailed extends DomainEvent {
    private final NotificationId notificationId;
    private final NotificationType type;
    private final Email recipientEmail;
    private final String subject;
    private final String errorMessage;
    
    private NotificationFailed(NotificationId notificationId, NotificationType type, Email recipientEmail, String subject, String errorMessage) {
        super();
        this.notificationId = notificationId;
        this.type = type;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.errorMessage = errorMessage;
    }
    
    public static NotificationFailed create(NotificationId notificationId, NotificationType type, Email recipientEmail, String subject, String errorMessage) {
        return new NotificationFailed(notificationId, type, recipientEmail, subject, errorMessage);
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
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
