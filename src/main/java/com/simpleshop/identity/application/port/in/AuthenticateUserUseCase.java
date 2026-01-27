package com.simpleshop.identity.application.port.in;

import com.simpleshop.identity.application.command.AuthenticateUserCommand;
import com.simpleshop.identity.domain.model.User;

public interface AuthenticateUserUseCase {
    User authenticate(AuthenticateUserCommand command);
}
