package com.simpleshop.cart.infrastructure.adapter.out.persistence;

import com.simpleshop.cart.application.port.out.CartRepository;
import com.simpleshop.cart.domain.model.Cart;
import com.simpleshop.cart.domain.model.vo.CartId;
import com.simpleshop.cart.domain.model.vo.SessionId;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CartRepositoryAdapter implements CartRepository {
    
    private final CartJpaRepository jpaRepository;
    
    public CartRepositoryAdapter(CartJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Cart save(Cart cart) {
        return jpaRepository.save(cart);
    }

    @Override
    public Optional<Cart> findById(CartId id) {
        return jpaRepository.findByIdWithItems(id.getValue());
    }

    @Override
    public Optional<Cart> findBySessionId(SessionId sessionId) {
        return jpaRepository.findBySessionIdValue(sessionId.getValue());
    }

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public void deleteById(CartId id) {
        jpaRepository.deleteById(id.getValue());
    }
}
