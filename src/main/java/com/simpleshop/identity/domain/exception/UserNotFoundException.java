package com.simpleshop.identity.domain.exception;

import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.shared.domain.exception.DomainException;
import com.simpleshop.shared.domain.model.vo.Email;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(UserId userId) {
        super("User not found: " + userId);
    }
    
    public UserNotFoundException(Email email) {
        super("User not found with email: " + email);
    }
}
