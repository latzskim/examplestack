package com.simpleshop.cart.infrastructure.adapter.out.persistence;

import com.simpleshop.cart.domain.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartJpaRepository extends JpaRepository<Cart, UUID> {
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.sessionId.value = :sessionId")
    Optional<Cart> findBySessionIdValue(@Param("sessionId") String sessionId);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<Cart> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(@Param("id") UUID id);
}
