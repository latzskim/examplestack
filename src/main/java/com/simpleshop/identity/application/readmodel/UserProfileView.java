package com.simpleshop.identity.application.readmodel;

import com.simpleshop.identity.domain.model.User;

public record UserProfileView(
    String email,
    String firstName,
    String lastName,
    String fullName,
    boolean isAdmin
) {
    public static UserProfileView fromUser(User user) {
        return new UserProfileView(
            user.getEmail().getValue(),
            user.getName().getFirstName(),
            user.getName().getLastName(),
            user.getName().getFullName(),
            user.isAdmin()
        );
    }
}
