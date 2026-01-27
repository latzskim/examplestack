package com.simpleshop.identity.application.readmodel;

import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.UserRole;
import com.simpleshop.identity.domain.model.vo.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserView(
    UUID id,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    UserStatus status,
    Instant createdAt,
    Instant lastLoginAt
) {
    public static UserView fromUser(User user) {
        return new UserView(
            user.getUserId().getValue(),
            user.getEmail().getValue(),
            user.getName().getFirstName(),
            user.getName().getLastName(),
            user.getRole(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }
    
    public String getFullName() {
        if (firstName == null && lastName == null) return "";
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return firstName + " " + lastName;
    }
}
