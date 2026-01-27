package com.simpleshop.identity.domain.event;

import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.shared.domain.model.DomainEvent;

public final class UserLoggedIn extends DomainEvent {
    private final UserId userId;
    
    public UserLoggedIn(UserId userId) {
        super();
        this.userId = userId;
    }
    
    public UserId getUserId() {
        return userId;
    }
}
