package com.simpleshop.identity.application.command;

public record AuthenticateUserCommand(
    String email,
    String password
) {
    public AuthenticateUserCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
