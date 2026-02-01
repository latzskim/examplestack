package com.simpleshop.identity.application.service;

import com.simpleshop.identity.application.port.in.DeactivateUserUseCase;
import com.simpleshop.identity.application.port.out.UserRepository;
import com.simpleshop.identity.domain.exception.UserNotFoundException;
import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.UserId;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserManagementService implements DeactivateUserUseCase {
    
    private final UserRepository userRepository;
    
    public UserManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    @WithSpan("identity.deactivateUser")
    public void deactivate(UserId userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        user.deactivate();
        userRepository.save(user);
    }
}
