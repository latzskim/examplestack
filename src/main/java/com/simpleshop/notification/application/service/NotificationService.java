package com.simpleshop.notification.application.service;

import com.simpleshop.notification.application.port.out.EmailSender;
import com.simpleshop.notification.application.port.out.InvoiceGenerator;
import com.simpleshop.notification.application.port.out.NotificationLogRepository;
import com.simpleshop.notification.application.port.out.OrderQueryPort;
import com.simpleshop.notification.application.port.out.ShipmentQueryPort;
import com.simpleshop.notification.application.port.out.UserQueryPort;
import com.simpleshop.notification.domain.event.NotificationFailed;
import com.simpleshop.notification.domain.event.NotificationSent;
import com.simpleshop.notification.domain.model.NotificationLog;
import com.simpleshop.notification.domain.model.vo.NotificationId;
import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.order.application.query.OrderView;
import com.simpleshop.shared.domain.model.vo.Email;
import com.simpleshop.shipping.application.query.ShipmentView;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final NotificationLogRepository notificationLogRepository;
    private final EmailSender emailSender;
    private final OrderQueryPort orderQueryPort;
    private final ShipmentQueryPort shipmentQueryPort;
    private final UserQueryPort userQueryPort;
    private final InvoiceGenerator invoiceGenerator;
    private final ApplicationEventPublisher eventPublisher;
    
    public NotificationService(
            NotificationLogRepository notificationLogRepository,
            EmailSender emailSender,
            OrderQueryPort orderQueryPort,
            ShipmentQueryPort shipmentQueryPort,
            UserQueryPort userQueryPort,
            InvoiceGenerator invoiceGenerator,
            ApplicationEventPublisher eventPublisher) {
        this.notificationLogRepository = notificationLogRepository;
        this.emailSender = emailSender;
        this.orderQueryPort = orderQueryPort;
        this.shipmentQueryPort = shipmentQueryPort;
        this.userQueryPort = userQueryPort;
        this.invoiceGenerator = invoiceGenerator;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @WithSpan("notification.sendOrderConfirmation")
    public void sendOrderConfirmation(@SpanAttribute("orderId") UUID orderId, Email recipientEmail, UUID userId) {
        Optional<OrderView> orderOpt = orderQueryPort.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            logger.error("Order not found for order confirmation: {}", orderId);
            return;
        }
        
        OrderView order = orderOpt.get();
        String subject = "Order Confirmation - " + order.orderNumber();
        
        NotificationLog log = NotificationLog.createPending(
            NotificationType.ORDER_CONFIRMATION,
            recipientEmail,
            subject
        );
        
        notificationLogRepository.save(log);
        
        // Get user's first name if available
        String firstName = userQueryPort.getUserById(userId)
            .map(user -> user.firstName())
            .orElse(null);
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("order", order);
        templateData.put("recipientEmail", recipientEmail.getValue());
        templateData.put("firstName", firstName);
        
        try {
            emailSender.sendEmail(recipientEmail, subject, NotificationType.ORDER_CONFIRMATION, templateData);
            log.markAsSent();
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationSent.create(log.getId(), NotificationType.ORDER_CONFIRMATION, recipientEmail, subject));
            logger.info("Order confirmation sent for order: {}", orderId);
        } catch (Exception e) {
            log.markAsFailed(e.getMessage());
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationFailed.create(log.getId(), NotificationType.ORDER_CONFIRMATION, recipientEmail, subject, e.getMessage()));
            logger.error("Failed to send order confirmation for order: {}", orderId, e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @WithSpan("notification.sendShipmentNotification")
    public void sendShipmentNotification(@SpanAttribute("shipmentId") UUID shipmentId, Email recipientEmail) {
        Optional<ShipmentView> shipmentOpt = shipmentQueryPort.getShipmentById(shipmentId);
        if (shipmentOpt.isEmpty()) {
            logger.error("Shipment not found for shipment notification: {}", shipmentId);
            return;
        }
        
        ShipmentView shipment = shipmentOpt.get();
        String subject = "Shipment Update - Tracking: " + shipment.trackingNumber();
        
        NotificationLog log = NotificationLog.createPending(
            NotificationType.SHIPMENT_UPDATE,
            recipientEmail,
            subject
        );
        
        notificationLogRepository.save(log);
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("shipment", shipment);
        templateData.put("recipientEmail", recipientEmail.getValue());
        
        try {
            emailSender.sendEmail(recipientEmail, subject, NotificationType.SHIPMENT_UPDATE, templateData);
            log.markAsSent();
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationSent.create(log.getId(), NotificationType.SHIPMENT_UPDATE, recipientEmail, subject));
            logger.info("Shipment notification sent for shipment: {}", shipmentId);
        } catch (Exception e) {
            log.markAsFailed(e.getMessage());
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationFailed.create(log.getId(), NotificationType.SHIPMENT_UPDATE, recipientEmail, subject, e.getMessage()));
            logger.error("Failed to send shipment notification for shipment: {}", shipmentId, e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @WithSpan("notification.sendShipmentCreatedNotification")
    public void sendShipmentCreatedNotification(@SpanAttribute("shipmentId") UUID shipmentId, Email recipientEmail, String firstName) {
        Optional<ShipmentView> shipmentOpt = shipmentQueryPort.getShipmentById(shipmentId);
        if (shipmentOpt.isEmpty()) {
            logger.error("Shipment not found for shipment created notification: {}", shipmentId);
            return;
        }
        
        ShipmentView shipment = shipmentOpt.get();
        String subject = "Your Order Has Shipped - Tracking: " + shipment.trackingNumber();
        
        NotificationLog log = NotificationLog.createPending(
            NotificationType.SHIPMENT_CREATED,
            recipientEmail,
            subject
        );
        
        notificationLogRepository.save(log);
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("shipment", shipment);
        templateData.put("recipientEmail", recipientEmail.getValue());
        templateData.put("firstName", firstName);
        templateData.put("trackingUrl", "/shipments/track?trackingNumber=" + shipment.trackingNumber());
        templateData.put("currentStatus", shipment.status().getDisplayName());
        templateData.put("isCreated", true);
        
        try {
            emailSender.sendEmail(recipientEmail, subject, NotificationType.SHIPMENT_CREATED, templateData);
            log.markAsSent();
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationSent.create(log.getId(), NotificationType.SHIPMENT_CREATED, recipientEmail, subject));
            logger.info("Shipment created notification sent for shipment: {}", shipmentId);
        } catch (Exception e) {
            log.markAsFailed(e.getMessage());
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationFailed.create(log.getId(), NotificationType.SHIPMENT_CREATED, recipientEmail, subject, e.getMessage()));
            logger.error("Failed to send shipment created notification for shipment: {}", shipmentId, e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @WithSpan("notification.sendShipmentStatusUpdateNotification")
    public void sendShipmentStatusUpdateNotification(@SpanAttribute("shipmentId") UUID shipmentId, Email recipientEmail, String firstName,
                                                      com.simpleshop.shipping.domain.model.vo.ShipmentStatus newStatus,
                                                      com.simpleshop.shipping.domain.model.vo.ShipmentStatus previousStatus,
                                                      String location, String notes) {
        Optional<ShipmentView> shipmentOpt = shipmentQueryPort.getShipmentById(shipmentId);
        if (shipmentOpt.isEmpty()) {
            logger.error("Shipment not found for status update notification: {}", shipmentId);
            return;
        }
        
        ShipmentView shipment = shipmentOpt.get();
        String subject = "Shipment Update - Tracking: " + shipment.trackingNumber();
        
        NotificationLog log = NotificationLog.createPending(
            NotificationType.SHIPMENT_UPDATE,
            recipientEmail,
            subject
        );
        
        notificationLogRepository.save(log);
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("shipment", shipment);
        templateData.put("recipientEmail", recipientEmail.getValue());
        templateData.put("firstName", firstName);
        templateData.put("trackingUrl", "/shipments/track?trackingNumber=" + shipment.trackingNumber());
        templateData.put("currentStatus", newStatus.getDisplayName());
        templateData.put("previousStatus", previousStatus.getDisplayName());
        templateData.put("location", location);
        templateData.put("notes", notes);
        templateData.put("isUpdate", true);
        
        try {
            emailSender.sendEmail(recipientEmail, subject, NotificationType.SHIPMENT_UPDATE, templateData);
            log.markAsSent();
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationSent.create(log.getId(), NotificationType.SHIPMENT_UPDATE, recipientEmail, subject));
            logger.info("Shipment status update notification sent for shipment: {} - status: {}", shipmentId, newStatus);
        } catch (Exception e) {
            log.markAsFailed(e.getMessage());
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationFailed.create(log.getId(), NotificationType.SHIPMENT_UPDATE, recipientEmail, subject, e.getMessage()));
            logger.error("Failed to send shipment status update notification for shipment: {}", shipmentId, e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @WithSpan("notification.sendInvoice")
    public void sendInvoice(@SpanAttribute("orderId") UUID orderId, Email recipientEmail) {
        Optional<OrderView> orderOpt = orderQueryPort.getOrderById(orderId);
        if (orderOpt.isEmpty()) {
            logger.error("Order not found for invoice: {}", orderId);
            return;
        }
        
        OrderView order = orderOpt.get();
        String subject = "Invoice for Order " + order.orderNumber();
        
        NotificationLog log = NotificationLog.createPending(
            NotificationType.INVOICE,
            recipientEmail,
            subject
        );
        
        notificationLogRepository.save(log);
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("order", order);
        templateData.put("recipientEmail", recipientEmail.getValue());
        
        try {
            byte[] invoicePdf = invoiceGenerator.generateInvoice(order);
            templateData.put("hasInvoice", true);
            emailSender.sendEmail(recipientEmail, subject, NotificationType.INVOICE, templateData);
            log.markAsSent();
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationSent.create(log.getId(), NotificationType.INVOICE, recipientEmail, subject));
            logger.info("Invoice sent for order: {}", orderId);
        } catch (Exception e) {
            log.markAsFailed(e.getMessage());
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationFailed.create(log.getId(), NotificationType.INVOICE, recipientEmail, subject, e.getMessage()));
            logger.error("Failed to send invoice for order: {}", orderId, e);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @WithSpan("notification.sendWelcomeEmail")
    public void sendWelcomeEmail(Email recipientEmail, String firstName) {
        String subject = "Welcome to Simple Shop!";
        
        NotificationLog log = NotificationLog.createPending(
            NotificationType.USER_WELCOME,
            recipientEmail,
            subject
        );
        
        notificationLogRepository.save(log);
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("firstName", firstName);
        templateData.put("recipientEmail", recipientEmail.getValue());
        
        try {
            emailSender.sendEmail(recipientEmail, subject, NotificationType.USER_WELCOME, templateData);
            log.markAsSent();
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationSent.create(log.getId(), NotificationType.USER_WELCOME, recipientEmail, subject));
            logger.info("Welcome email sent to: {}", recipientEmail.getValue());
        } catch (Exception e) {
            log.markAsFailed(e.getMessage());
            notificationLogRepository.save(log);
            eventPublisher.publishEvent(NotificationFailed.create(log.getId(), NotificationType.USER_WELCOME, recipientEmail, subject, e.getMessage()));
            logger.error("Failed to send welcome email to: {}", recipientEmail.getValue(), e);
        }
    }
}
