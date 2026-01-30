package com.simpleshop.notification.infrastructure.adapter.out.persistence;

import com.simpleshop.identity.application.port.in.GetUserUseCase;
import com.simpleshop.identity.application.query.GetUserByEmailQuery;
import com.simpleshop.identity.application.query.GetUserQuery;
import com.simpleshop.identity.application.readmodel.UserView;
import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.notification.application.port.out.UserQueryPort;
import com.simpleshop.shared.domain.model.vo.Email;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserQueryAdapter implements UserQueryPort {
    
    private final GetUserUseCase getUserUseCase;
    
    public UserQueryAdapter(GetUserUseCase getUserUseCase) {
        this.getUserUseCase = getUserUseCase;
    }
    
    @Override
    public Optional<UserView> getUserById(UUID userId) {
        return getUserUseCase.getUser(new GetUserQuery(UserId.of(userId)))
            .map(UserView::fromUser);
    }
    
    @Override
    public Optional<UserView> getUserByEmail(Email email) {
        return getUserUseCase.getUserByEmail(new GetUserByEmailQuery(email))
            .map(UserView::fromUser);
    }
}
