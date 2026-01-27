package com.simpleshop.identity.infrastructure.adapter.out.persistence;

import com.simpleshop.identity.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailValue(String email);
    boolean existsByEmailValue(String email);
}
