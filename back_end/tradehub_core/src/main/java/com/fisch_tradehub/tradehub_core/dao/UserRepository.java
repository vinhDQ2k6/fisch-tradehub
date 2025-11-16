package com.fisch_tradehub.tradehub_core.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisch_tradehub.tradehub_core.web.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}