package com.fisch_tradehub.tradehub_core.security;

import java.util.Objects;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;

@Service
public class UserDetailsManager implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Objects.requireNonNull(username, "username must not be null");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                // sẽ thành ROLE_ADMIN, ROLE_USER ở bên trong
                .roles(user.getRole())
                .disabled(!user.isActive())
                .build();
    }
}