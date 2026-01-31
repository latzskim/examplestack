package com.simpleshop.notification.infrastructure.adapter.out.email;

import com.simpleshop.notification.application.port.out.EmailSender;
import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.shared.domain.model.vo.Email;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import io.micrometer.tracing.annotation.NewSpan;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
public class SpringMailEmailSender implements EmailSender {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringMailEmailSender.class);
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromAddress;
    private final String baseUrl;
    
    public SpringMailEmailSender(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            @Value("${spring.mail.from:noreply@simpleshop.com}") String fromAddress,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromAddress = fromAddress;
        this.baseUrl = baseUrl;
    }
    
    @Override
    @NewSpan
    public void sendEmail(Email recipient, String subject, NotificationType type, Map<String, Object> templateData) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromAddress);
            helper.setTo(recipient.getValue());
            helper.setSubject(subject);
            
            // Create Thymeleaf context with template data
            Context context = new Context();
            context.setVariable("baseUrl", baseUrl);
            templateData.forEach(context::setVariable);
            
            // Determine template name based on notification type
            String templateName = getTemplateName(type);
            String htmlContent = templateEngine.process("email/" + templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.debug("Email sent successfully to: {} with subject: {}", recipient.getValue(), subject);
            
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", recipient.getValue(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    private String getTemplateName(NotificationType type) {
        return switch (type) {
            case ORDER_CONFIRMATION -> "order-confirmation";
            case SHIPMENT_UPDATE -> "shipment-update";
            case SHIPMENT_CREATED -> "shipment-created";
            case INVOICE -> "invoice";
            case USER_WELCOME -> "welcome";
        };
    }
}
