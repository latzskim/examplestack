package com.simpleshop.notification.domain.model.vo;

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED;
    
    public boolean isFinal() {
        return this == SENT || this == FAILED;
    }
}
