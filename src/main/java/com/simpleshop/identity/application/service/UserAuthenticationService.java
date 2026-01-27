package com.simpleshop.identity.application.service;

import com.simpleshop.identity.application.command.AuthenticateUserCommand;
import com.simpleshop.identity.application.port.in.AuthenticateUserUseCase;
import com.simpleshop.identity.application.port.out.PasswordEncoderPort;
import com.simpleshop.identity.application.port.out.UserRepository;
import com.simpleshop.identity.domain.exception.InvalidCredentialsException;
import com.simpleshop.identity.domain.exception.UserInactiveException;
import com.simpleshop.identity.domain.model.User;
import com.simpleshop.shared.domain.model.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAuthenticationService implements AuthenticateUserUseCase {
    
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    
    public UserAuthenticationService(UserRepository userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public User authenticate(AuthenticateUserCommand command) {
        Email email = Email.of(command.email());
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);
        
        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        
        if (!user.isActive()) {
            throw new UserInactiveException();
        }
        
        user.recordLogin();
        userRepository.save(user);
        
        return user;
    }
}
