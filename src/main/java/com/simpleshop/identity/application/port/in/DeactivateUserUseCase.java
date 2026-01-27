package com.simpleshop.identity.application.port.in;

import com.simpleshop.identity.domain.model.vo.UserId;

public interface DeactivateUserUseCase {
    void deactivate(UserId userId);
}
