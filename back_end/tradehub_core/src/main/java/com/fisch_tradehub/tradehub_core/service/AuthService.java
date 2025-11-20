package com.fisch_tradehub.tradehub_core.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.RegisterRequest;
import com.fisch_tradehub.tradehub_core.web.dto.UserDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role("USER")
                .active(true)
                .build();

        user = userRepository.save(user);

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                List.of(user.getRole())
        );
    }

    public UserDTO getUserDto(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                List.of(user.getRole())
        );
    }
}
