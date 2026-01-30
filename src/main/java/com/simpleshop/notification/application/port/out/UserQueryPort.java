package com.simpleshop.notification.application.port.out;

import com.simpleshop.identity.application.readmodel.UserView;
import com.simpleshop.shared.domain.model.vo.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserQueryPort {
    
    Optional<UserView> getUserById(UUID userId);
    
    Optional<UserView> getUserByEmail(Email email);
}
