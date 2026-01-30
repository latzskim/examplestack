package com.simpleshop.shipping.infrastructure.adapter.out.persistence;

import com.simpleshop.shipping.application.port.out.TrackingNumberGenerator;
import com.simpleshop.shipping.domain.model.vo.TrackingNumber;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class DatabaseTrackingNumberGenerator implements TrackingNumberGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseTrackingNumberGenerator.class);
    private static final String SEQUENCE_NAME = "tracking_number_seq";
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureSequenceExists() {
        try {
            entityManager.createNativeQuery(
                "CREATE SEQUENCE IF NOT EXISTS " + SEQUENCE_NAME + 
                " START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1"
            ).executeUpdate();
            log.info("Tracking number sequence ensured: {}", SEQUENCE_NAME);
        } catch (Exception e) {
            log.warn("Sequence creation failed: {}", e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public TrackingNumber generate() {
        Long nextVal = (Long) entityManager
            .createNativeQuery("SELECT nextval('" + SEQUENCE_NAME + "')")
            .getSingleResult();
        
        int year = LocalDate.now().getYear();
        String trackingNumberValue = String.format("SHIP-%d-%05d", year, nextVal);
        
        return TrackingNumber.of(trackingNumberValue);
    }
}
