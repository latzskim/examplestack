package com.simpleshop.notification.infrastructure.adapter.in.event;

import com.simpleshop.identity.application.readmodel.UserView;
import com.simpleshop.identity.domain.event.UserRegistered;
import com.simpleshop.notification.application.port.out.OrderQueryPort;
import com.simpleshop.notification.application.port.out.ShipmentQueryPort;
import com.simpleshop.notification.application.port.out.UserQueryPort;
import com.simpleshop.notification.application.service.NotificationService;
import com.simpleshop.order.application.query.OrderView;
import com.simpleshop.order.domain.event.OrderConfirmed;
import com.simpleshop.shared.domain.model.vo.Email;
import com.simpleshop.shipping.application.query.ShipmentView;
import com.simpleshop.shipping.domain.event.ShipmentCreated;
import com.simpleshop.shipping.domain.event.ShipmentStatusUpdated;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles domain events for sending email notifications.
 * 
 * Notification flow:
 * - User registration: welcome email
 * - Order confirmed: order confirmation + invoice
 * - Shipment created: tracking link with timeline
 * - Shipment status updated: status update with timeline and tracking link
 */
@Component
public class NotificationEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);
    
    private final NotificationService notificationService;
    private final UserQueryPort userQueryPort;
    private final OrderQueryPort orderQueryPort;
    private final ShipmentQueryPort shipmentQueryPort;
    
    public NotificationEventListener(NotificationService notificationService,
                                     UserQueryPort userQueryPort,
                                     OrderQueryPort orderQueryPort,
                                     ShipmentQueryPort shipmentQueryPort) {
        this.notificationService = notificationService;
        this.userQueryPort = userQueryPort;
        this.orderQueryPort = orderQueryPort;
        this.shipmentQueryPort = shipmentQueryPort;
    }
    
    @ApplicationModuleListener
    @WithSpan
    public void onUserRegistered(UserRegistered event) {
        logger.info("User registered: {} - sending welcome email", event.getUserId());
        
        Optional<UserView> userOpt = userQueryPort.getUserById(event.getUserId().getValue());
        String firstName = userOpt.map(UserView::firstName).orElse("");
        
        try {
            notificationService.sendWelcomeEmail(event.getEmail(), firstName);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", event.getEmail().getValue(), e);
        }
    }
    
    @ApplicationModuleListener
    @WithSpan
    public void onOrderConfirmed(OrderConfirmed event) {
        logger.info("Order confirmed: {} - sending notifications", event.getOrderNumber());
        
        Optional<UserView> userOpt = userQueryPort.getUserById(event.getUserId());
        if (userOpt.isEmpty()) {
            logger.error("User not found for order confirmation: {}", event.getUserId());
            return;
        }
        
        UserView user = userOpt.get();
        
        // Send order confirmation
        try {
            notificationService.sendOrderConfirmation(event.getOrderId(), 
                Email.of(user.email()),
                event.getUserId());
        } catch (Exception e) {
            logger.error("Failed to send order confirmation for order: {}", event.getOrderNumber(), e);
        }
        
        // Send invoice
        try {
            notificationService.sendInvoice(event.getOrderId(),
                Email.of(user.email()));
        } catch (Exception e) {
            logger.error("Failed to send invoice for order: {}", event.getOrderNumber(), e);
        }
    }
    
    @ApplicationModuleListener
    @WithSpan
    public void onShipmentCreated(ShipmentCreated event) {
        logger.info("Shipment created: {} - sending tracking notification", event.getTrackingNumber());
        
        // Get order to find user's email
        Optional<OrderView> orderOpt = orderQueryPort.getOrderById(event.getOrderId());
        if (orderOpt.isEmpty()) {
            logger.error("Order not found for shipment notification: {}", event.getOrderId());
            return;
        }
        
        OrderView order = orderOpt.get();
        Optional<UserView> userOpt = userQueryPort.getUserById(order.userId());
        if (userOpt.isEmpty()) {
            logger.error("User not found for shipment notification: {}", order.userId());
            return;
        }
        
        UserView user = userOpt.get();
        
        try {
            notificationService.sendShipmentCreatedNotification(
                event.getShipmentId(),
                Email.of(user.email()),
                user.firstName()
            );
        } catch (Exception e) {
            logger.error("Failed to send shipment created notification for tracking: {}", 
                event.getTrackingNumber(), e);
        }
    }
    
    @ApplicationModuleListener
    @WithSpan
    public void onShipmentStatusUpdated(ShipmentStatusUpdated event) {
        logger.info("Shipment status updated: {} from {} to {}", 
            event.getTrackingNumber(), 
            event.getPreviousStatus(), 
            event.getNewStatus());
        
        // Only send notifications for important status changes
        if (!shouldNotifyStatusChange(event.getNewStatus())) {
            return;
        }
        
        // Get shipment to find order
        Optional<ShipmentView> shipmentOpt = shipmentQueryPort.getShipmentById(event.getShipmentId());
        if (shipmentOpt.isEmpty()) {
            logger.error("Shipment not found for status update notification: {}", event.getShipmentId());
            return;
        }
        
        ShipmentView shipment = shipmentOpt.get();
        
        // Get order to find user's email
        Optional<OrderView> orderOpt = orderQueryPort.getOrderById(shipment.orderId());
        if (orderOpt.isEmpty()) {
            logger.error("Order not found for shipment status notification: {}", shipment.orderId());
            return;
        }
        
        OrderView order = orderOpt.get();
        Optional<UserView> userOpt = userQueryPort.getUserById(order.userId());
        if (userOpt.isEmpty()) {
            logger.error("User not found for shipment status notification: {}", order.userId());
            return;
        }
        
        UserView user = userOpt.get();
        
        try {
            notificationService.sendShipmentStatusUpdateNotification(
                event.getShipmentId(),
                Email.of(user.email()),
                user.firstName(),
                event.getNewStatus(),
                event.getPreviousStatus(),
                event.getLocation(),
                event.getNotes()
            );
        } catch (Exception e) {
            logger.error("Failed to send shipment status update notification for tracking: {}", 
                event.getTrackingNumber(), e);
        }
    }
    
    private boolean shouldNotifyStatusChange(com.simpleshop.shipping.domain.model.vo.ShipmentStatus status) {
        return switch (status) {
            case SHIPPED, OUT_FOR_DELIVERY, DELIVERED, FAILED -> true;
            default -> false;
        };
    }
}
