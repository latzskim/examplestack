package com.simpleshop.identity.domain.event;

import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.shared.domain.model.DomainEvent;

public final class UserDeactivated extends DomainEvent {
    private final UserId userId;
    
    public UserDeactivated(UserId userId) {
        super();
        this.userId = userId;
    }
    
    public UserId getUserId() {
        return userId;
    }
}
