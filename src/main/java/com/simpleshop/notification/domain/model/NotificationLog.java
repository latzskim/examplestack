package com.simpleshop.notification.domain.model;

import com.simpleshop.notification.domain.model.vo.NotificationId;
import com.simpleshop.notification.domain.model.vo.NotificationStatus;
import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.shared.domain.model.vo.Email;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notification_logs")
public class NotificationLog {
    
    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "id"))
    })
    private NotificationId id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;
    
    @Embedded
    private Email recipientEmail;
    
    @Column(name = "subject", length = 500)
    private String subject;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;
    
    @Column(name = "sent_at")
    private Instant sentAt;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    protected NotificationLog() {}
    
    private NotificationLog(NotificationId id, NotificationType type, Email recipientEmail, String subject) {
        this.id = id;
        this.type = type;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.status = NotificationStatus.PENDING;
        this.createdAt = Instant.now();
    }
    
    public static NotificationLog createPending(NotificationType type, Email recipientEmail, String subject) {
        return new NotificationLog(NotificationId.generate(), type, recipientEmail, subject);
    }
    
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }
    
    public NotificationId getId() {
        return id;
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
    
    public NotificationStatus getStatus() {
        return status;
    }
    
    public Instant getSentAt() {
        return sentAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }
}
