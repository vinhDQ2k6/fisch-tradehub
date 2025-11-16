package com.fisch_tradehub.tradehub_core.security;

import java.util.Objects;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fisch_tradehub.tradehub_core.dao.UserRepository;
import com.fisch_tradehub.tradehub_core.web.model.User;

@Service
public class UserDetailsManager implements UserDetailsService {

    private final UserRepository dao;

    public UserDetailsManager(UserRepository userRepository) {
        this.dao = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Objects.requireNonNull(username, "username must not be null");

        User user = dao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // DB lưu: "ADMIN", "USER", ...
        String role = user.getRole();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                // sẽ thành ROLE_ADMIN, ROLE_USER ở bên trong
                .roles(role)
                .disabled(!user.isActive())
                .build();
    }
}