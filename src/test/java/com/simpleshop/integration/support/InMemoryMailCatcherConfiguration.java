package com.simpleshop.integration.support;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
@Profile("test")
public class InMemoryMailCatcherConfiguration {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return new InMemoryMailSender();
    }

    static class InMemoryMailSender implements JavaMailSender {

        private final List<MimeMessage> sentMimeMessages = new CopyOnWriteArrayList<>();
        private final List<SimpleMailMessage> sentSimpleMessages = new CopyOnWriteArrayList<>();

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(Session.getInstance(new Properties()));
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            try {
                return new MimeMessage(Session.getInstance(new Properties()), contentStream);
            } catch (MessagingException e) {
                throw new MailParseException("Failed to create MimeMessage", e);
            }
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            try {
                mimeMessage.saveChanges();
                sentMimeMessages.add(mimeMessage);
            } catch (MessagingException e) {
                throw new MailSendException("Failed to store MimeMessage", e);
            }
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            for (MimeMessage mimeMessage : mimeMessages) {
                send(mimeMessage);
            }
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            sentSimpleMessages.add(simpleMessage);
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            for (SimpleMailMessage simpleMessage : simpleMessages) {
                send(simpleMessage);
            }
        }

        List<MimeMessage> getSentMimeMessages() {
            return sentMimeMessages;
        }

        List<SimpleMailMessage> getSentSimpleMessages() {
            return sentSimpleMessages;
        }
    }
}
