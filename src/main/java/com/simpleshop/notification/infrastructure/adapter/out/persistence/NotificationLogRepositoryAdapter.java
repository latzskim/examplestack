package com.simpleshop.notification.infrastructure.adapter.out.persistence;

import com.simpleshop.notification.application.port.out.NotificationLogRepository;
import com.simpleshop.notification.domain.model.NotificationLog;
import com.simpleshop.notification.domain.model.vo.NotificationId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotificationLogRepositoryAdapter implements NotificationLogRepository {
    
    private final JpaNotificationLogRepository jpaRepository;
    
    public NotificationLogRepositoryAdapter(JpaNotificationLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public NotificationLog save(NotificationLog notificationLog) {
        return jpaRepository.save(notificationLog);
    }
    
    @Override
    public Optional<NotificationLog> findById(NotificationId id) {
        return jpaRepository.findById(id);
    }
}
