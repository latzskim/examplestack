package com.simpleshop.identity.domain.event;

import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.shared.domain.model.DomainEvent;
import com.simpleshop.shared.domain.model.vo.Email;

public final class UserRegistered extends DomainEvent {
    private final UserId userId;
    private final Email email;
    
    public UserRegistered(UserId userId, Email email) {
        super();
        this.userId = userId;
        this.email = email;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public Email getEmail() {
        return email;
    }
}
