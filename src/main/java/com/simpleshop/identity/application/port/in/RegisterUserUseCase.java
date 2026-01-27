package com.simpleshop.identity.application.port.in;

import com.simpleshop.identity.application.command.RegisterUserCommand;
import com.simpleshop.identity.domain.model.vo.UserId;

public interface RegisterUserUseCase {
    UserId register(RegisterUserCommand command);
}
