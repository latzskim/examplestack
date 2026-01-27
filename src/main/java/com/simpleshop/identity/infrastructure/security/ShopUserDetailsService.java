package com.simpleshop.identity.infrastructure.security;

import com.simpleshop.identity.application.port.out.UserRepository;
import com.simpleshop.identity.domain.model.User;
import com.simpleshop.shared.domain.model.vo.Email;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public ShopUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Email email = Email.of(username);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new ShopUserDetails(user);
    }
}
