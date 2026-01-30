package com.simpleshop.notification.infrastructure.adapter.out.persistence;

import com.simpleshop.notification.domain.model.NotificationLog;
import com.simpleshop.notification.domain.model.vo.NotificationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationLogRepository extends JpaRepository<NotificationLog, NotificationId> {
}
