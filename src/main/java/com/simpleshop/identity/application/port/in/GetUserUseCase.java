package com.simpleshop.identity.application.port.in;

import com.simpleshop.identity.application.query.GetUserByEmailQuery;
import com.simpleshop.identity.application.query.GetUserQuery;
import com.simpleshop.identity.domain.model.User;

import java.util.Optional;

public interface GetUserUseCase {
    Optional<User> getUser(GetUserQuery query);
    Optional<User> getUserByEmail(GetUserByEmailQuery query);
}
