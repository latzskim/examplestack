package com.simpleshop.identity.application.command;

public record RegisterUserCommand(
    String email,
    String password,
    String firstName,
    String lastName
) {
    public RegisterUserCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}
