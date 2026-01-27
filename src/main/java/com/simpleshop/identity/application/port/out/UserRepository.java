package com.simpleshop.identity.application.port.out;

import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.shared.domain.model.vo.Email;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
