package com.fisch_tradehub.tradehub_core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.entity.Cart;
import com.fisch_tradehub.tradehub_core.entity.User;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUser(User user);

    List<Cart> findByUserId(Long userId);

    Optional<Cart> findByUserIdAndFishId(Long userId, Long fishId);

    void deleteByUserId(Long userId);
}