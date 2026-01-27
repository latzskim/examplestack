package com.simpleshop.identity.application.service;

import com.simpleshop.identity.application.port.in.GetUserUseCase;
import com.simpleshop.identity.application.port.out.UserRepository;
import com.simpleshop.identity.application.query.GetUserByEmailQuery;
import com.simpleshop.identity.application.query.GetUserQuery;
import com.simpleshop.identity.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserQueryService implements GetUserUseCase {
    
    private final UserRepository userRepository;
    
    public UserQueryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public Optional<User> getUser(GetUserQuery query) {
        return userRepository.findById(query.userId());
    }
    
    @Override
    public Optional<User> getUserByEmail(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }
}
