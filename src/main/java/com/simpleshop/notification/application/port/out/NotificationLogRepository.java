package com.simpleshop.notification.application.port.out;

import com.simpleshop.notification.domain.model.NotificationLog;
import com.simpleshop.notification.domain.model.vo.NotificationId;
import java.util.Optional;

public interface NotificationLogRepository {
    
    NotificationLog save(NotificationLog notificationLog);
    
    Optional<NotificationLog> findById(NotificationId id);
}
