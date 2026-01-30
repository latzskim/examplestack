package com.simpleshop.order.infrastructure.adapter.out.persistence;

import com.simpleshop.order.application.port.out.OrderNumberGenerator;
import com.simpleshop.order.domain.model.vo.OrderNumber;
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
public class DatabaseOrderNumberGenerator implements OrderNumberGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseOrderNumberGenerator.class);
    private static final String SEQUENCE_NAME = "order_number_seq";
    
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
            log.info("Order number sequence ensured: {}", SEQUENCE_NAME);
        } catch (Exception e) {
            log.warn("Sequence creation failed: {}", e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public OrderNumber generate() {
        Long nextVal = (Long) entityManager
            .createNativeQuery("SELECT nextval('" + SEQUENCE_NAME + "')")
            .getSingleResult();
        
        int year = LocalDate.now().getYear();
        String orderNumberValue = String.format("ORD-%d-%05d", year, nextVal);
        
        return OrderNumber.of(orderNumberValue);
    }
}
