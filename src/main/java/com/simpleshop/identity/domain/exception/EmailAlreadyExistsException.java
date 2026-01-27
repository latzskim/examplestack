package com.simpleshop.identity.domain.exception;

import com.simpleshop.shared.domain.exception.DomainException;
import com.simpleshop.shared.domain.model.vo.Email;

public class EmailAlreadyExistsException extends DomainException {
    public EmailAlreadyExistsException(Email email) {
        super("Email already registered: " + email);
    }
}
