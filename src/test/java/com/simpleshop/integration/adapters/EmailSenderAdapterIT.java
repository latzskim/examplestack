package com.simpleshop.integration.adapters;

import com.simpleshop.SimpleShopApplication;
import com.simpleshop.notification.domain.model.vo.NotificationType;
import com.simpleshop.notification.infrastructure.adapter.out.email.SpringMailEmailSender;
import com.simpleshop.order.application.query.OrderItemView;
import com.simpleshop.order.application.query.OrderView;
import com.simpleshop.shared.domain.model.vo.Email;
import com.simpleshop.shipping.application.query.ShipmentView;
import com.simpleshop.shipping.domain.model.vo.ShipmentStatus;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.TemplateEngine;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SimpleShopApplication.class)
@ActiveProfiles("test")
class EmailSenderAdapterIT {

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void intP106_shouldRenderTemplatesAndComposeMimeEmailsForAllNotificationTypes() throws Exception {
        CapturingMailSender mailSender = new CapturingMailSender();

        SpringMailEmailSender emailSender = new SpringMailEmailSender(
            mailSender,
            templateEngine,
            "noreply@test.simpleshop.com",
            "http://localhost:8080"
        );

        Email recipient = Email.of("customer@example.com");

        for (NotificationType type : NotificationType.values()) {
            String subject = subjectFor(type);
            Map<String, Object> templateData = templateDataFor(type, recipient.getValue());

            emailSender.sendEmail(recipient, subject, type, templateData);

            MimeMessage sent = mailSender.getLastSentMessage();
            assertNotNull(sent, "Expected a message to be captured for " + type);
            assertEquals(subject, sent.getSubject());

            Address[] from = sent.getFrom();
            assertNotNull(from);
            assertEquals(1, from.length);
            assertTrue(from[0].toString().contains("noreply@test.simpleshop.com"));

            Address[] recipients = sent.getRecipients(Message.RecipientType.TO);
            assertNotNull(recipients);
            assertEquals(1, recipients.length);
            assertTrue(recipients[0].toString().contains("customer@example.com"));

            String htmlBody = mailSender.getLastHtmlBody();
            assertNotNull(htmlBody);
            assertFalse(htmlBody.isBlank());
            assertContainsExpectedMarker(type, htmlBody);
        }
    }

    private Map<String, Object> templateDataFor(NotificationType type, String recipientEmail) {
        OrderView order = sampleOrder();
        ShipmentView shipment = sampleShipment();

        Map<String, Object> data = new HashMap<>();
        data.put("recipientEmail", recipientEmail);

        switch (type) {
            case ORDER_CONFIRMATION -> {
                data.put("order", order);
                data.put("firstName", "Taylor");
            }
            case INVOICE -> data.put("order", order);
            case SHIPMENT_CREATED -> {
                data.put("shipment", shipment);
                data.put("firstName", "Taylor");
                data.put("currentStatus", shipment.status().getDisplayName());
                data.put("trackingUrl", "/shipments/track?trackingNumber=" + shipment.trackingNumber());
                data.put("isCreated", true);
            }
            case SHIPMENT_UPDATE -> {
                data.put("shipment", shipment);
                data.put("firstName", "Taylor");
                data.put("currentStatus", shipment.status().getDisplayName());
                data.put("previousStatus", "Packed");
                data.put("location", "Distribution Center");
                data.put("notes", "Loaded onto truck");
                data.put("trackingUrl", "/shipments/track?trackingNumber=" + shipment.trackingNumber());
                data.put("isUpdate", true);
            }
            case USER_WELCOME -> data.put("firstName", "Taylor");
        }

        return data;
    }

    private String subjectFor(NotificationType type) {
        return switch (type) {
            case ORDER_CONFIRMATION -> "Order Confirmation - ORD-2026-54321";
            case INVOICE -> "Invoice - ORD-2026-54321";
            case SHIPMENT_CREATED -> "Created - Tracking: SHIP-2026-54321";
            case SHIPMENT_UPDATE -> "Shipment Update - Tracking: SHIP-2026-54321";
            case USER_WELCOME -> "Welcome to Simple Shop!";
        };
    }

    private void assertContainsExpectedMarker(NotificationType type, String htmlBody) {
        switch (type) {
            case ORDER_CONFIRMATION -> {
                assertTrue(htmlBody.contains("Order Confirmation"));
                assertTrue(htmlBody.contains("ORD-2026-54321"));
            }
            case INVOICE -> {
                assertTrue(htmlBody.contains("INVOICE"));
                assertTrue(htmlBody.contains("ORD-2026-54321"));
            }
            case SHIPMENT_CREATED -> assertTrue(htmlBody.contains("SHIP-2026-54321"));
            case SHIPMENT_UPDATE -> {
                assertTrue(htmlBody.contains("Shipment Update"));
                assertTrue(htmlBody.contains("SHIP-2026-54321"));
            }
            case USER_WELCOME -> {
                assertTrue(htmlBody.contains("Welcome"));
                assertTrue(htmlBody.contains("customer@example.com"));
            }
        }
    }

    private OrderView sampleOrder() {
        return new OrderView(
            UUID.randomUUID(),
            "ORD-2026-54321",
            UUID.randomUUID(),
            List.of(
                new OrderItemView(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Desk Lamp",
                    2,
                    new BigDecimal("29.99"),
                    "USD",
                    new BigDecimal("59.98"),
                    UUID.randomUUID()
                )
            ),
            "100 Main St",
            "Seattle",
            "98101",
            "USA",
            "CONFIRMED",
            new BigDecimal("59.98"),
            "USD",
            2,
            Instant.now(),
            Instant.now(),
            null,
            null,
            null,
            null
        );
    }

    private ShipmentView sampleShipment() {
        return new ShipmentView(
            UUID.randomUUID(),
            "SHIP-2026-54321",
            UUID.randomUUID(),
            UUID.randomUUID(),
            "100 Main St",
            "Seattle",
            "98101",
            "USA",
            ShipmentStatus.SHIPPED,
            LocalDate.now().plusDays(3),
            Instant.now()
        );
    }

    private static class CapturingMailSender implements JavaMailSender {

        private MimeMessage lastSentMessage;

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(Session.getInstance(new Properties()));
        }

        @Override
        public MimeMessage createMimeMessage(java.io.InputStream contentStream) throws MailException {
            try {
                return new MimeMessage(Session.getInstance(new Properties()), contentStream);
            } catch (MessagingException e) {
                throw new MailSendException("Failed to create MimeMessage", e);
            }
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            try {
                mimeMessage.saveChanges();
            } catch (MessagingException e) {
                throw new MailSendException("Failed to save MimeMessage", e);
            }
            this.lastSentMessage = mimeMessage;
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            if (mimeMessages.length > 0) {
                send(mimeMessages[mimeMessages.length - 1]);
            }
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            throw new UnsupportedOperationException("SimpleMailMessage is not used in these tests");
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            throw new UnsupportedOperationException("SimpleMailMessage is not used in these tests");
        }

        MimeMessage getLastSentMessage() {
            return lastSentMessage;
        }

        String getLastHtmlBody() throws Exception {
            if (lastSentMessage == null) {
                return null;
            }

            Object content = lastSentMessage.getContent();
            return extractHtml(content);
        }

        private String extractHtml(Object content) throws Exception {
            if (content instanceof String body) {
                return body;
            }

            if (content instanceof Multipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    Part part = multipart.getBodyPart(i);
                    String contentType = part.getContentType();
                    Object partContent = part.getContent();

                    if (contentType != null && contentType.toLowerCase().contains("text/html")
                        && partContent instanceof String body) {
                        return body;
                    }

                    String nested = extractHtml(partContent);
                    if (nested != null && !nested.isBlank()) {
                        return nested;
                    }
                }
            }

            return null;
        }
    }
}
