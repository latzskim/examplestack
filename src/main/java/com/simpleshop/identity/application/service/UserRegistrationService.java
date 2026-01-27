package com.simpleshop.identity.application.service;

import com.simpleshop.identity.application.command.RegisterUserCommand;
import com.simpleshop.identity.application.port.in.RegisterUserUseCase;
import com.simpleshop.identity.application.port.out.PasswordEncoderPort;
import com.simpleshop.identity.application.port.out.UserRepository;
import com.simpleshop.identity.domain.exception.EmailAlreadyExistsException;
import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.PersonName;
import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.shared.domain.model.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserRegistrationService implements RegisterUserUseCase {
    
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    
    public UserRegistrationService(UserRepository userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserId register(RegisterUserCommand command) {
        Email email = Email.of(command.email());
        
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        
        String encodedPassword = passwordEncoder.encode(command.password());
        PersonName name = PersonName.of(command.firstName(), command.lastName());
        
        User user = User.register(email, encodedPassword, name);
        userRepository.save(user);
        
        return user.getUserId();
    }
}
