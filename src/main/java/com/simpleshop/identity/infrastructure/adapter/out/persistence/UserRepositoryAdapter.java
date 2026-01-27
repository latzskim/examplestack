package com.simpleshop.identity.infrastructure.adapter.out.persistence;

import com.simpleshop.identity.application.port.out.UserRepository;
import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.shared.domain.model.vo.Email;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    
    private final JpaUserRepository jpaRepository;
    
    public UserRepositoryAdapter(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }
    
    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.getValue());
    }
    
    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmailValue(email.getValue());
    }
    
    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmailValue(email.getValue());
    }
}
