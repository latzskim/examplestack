package com.simpleshop.notification.application.port.out;

import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.shared.domain.model.vo.Email;
import java.util.Map;

public interface EmailSender {
    
    void sendEmail(Email recipient, String subject, NotificationType type, Map<String, Object> templateData);
}
