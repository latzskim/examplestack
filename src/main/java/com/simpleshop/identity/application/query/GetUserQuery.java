package com.simpleshop.identity.application.query;

import com.simpleshop.identity.domain.model.vo.UserId;

public record GetUserQuery(UserId userId) {
    public GetUserQuery {
        if (userId == null) {
            throw new IllegalArgumentException("UserId is required");
        }
    }
}
