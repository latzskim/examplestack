package com.simpleshop.identity.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;

public class UserInactiveException extends DomainException {
    public UserInactiveException() {
        super("User account is inactive");
    }
}
