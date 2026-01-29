package com.simpleshop.cart.application.port.out;

import com.simpleshop.cart.domain.model.Cart;
import com.simpleshop.cart.domain.model.vo.CartId;
import com.simpleshop.cart.domain.model.vo.SessionId;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findById(CartId id);
    Optional<Cart> findBySessionId(SessionId sessionId);
    Optional<Cart> findByUserId(UUID userId);
    void deleteById(CartId id);
}
