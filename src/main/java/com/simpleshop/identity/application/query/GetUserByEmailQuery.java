package com.simpleshop.identity.application.query;

import com.simpleshop.shared.domain.model.vo.Email;

public record GetUserByEmailQuery(Email email) {
    public GetUserByEmailQuery {
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
    }
}
