package com.simpleshop.shared.domain.model;

import jakarta.persistence.MappedSuperclass;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.Collection;

@MappedSuperclass
public abstract class AggregateRoot<T extends AggregateRoot<T>> extends AbstractAggregateRoot<T> {
    
    public Collection<Object> getDomainEvents() {
        return domainEvents();
    }
    
    public void clearEvents() {
        clearDomainEvents();
    }
}
