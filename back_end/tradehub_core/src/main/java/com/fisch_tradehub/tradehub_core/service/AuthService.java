package com.fisch_tradehub.tradehub_core.service;

import com.fisch_tradehub.tradehub_core.common.Constants;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.exception.DuplicateResourceException;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.RegisterRequest;
import com.fisch_tradehub.tradehub_core.web.dto.UserDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user authentication and registration.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Register a new user account.
   *
   * @param request the registration details
   * @return the created user DTO
   * @throws DuplicateResourceException if username or email already exists
   */
  @Transactional
  public UserDTO register(RegisterRequest request) {
    if (userRepository.findByUsername(request.username()).isPresent()) {
      throw new DuplicateResourceException(Constants.USERNAME_EXISTS);
    }
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new DuplicateResourceException(Constants.EMAIL_EXISTS);
    }

    User user = User.builder()
      .username(request.username())
      .email(request.email())
      .password(passwordEncoder.encode(request.password()))
      .role(Constants.ROLE_USER)
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

  /**
   * Get user details by username.
   *
   * @param username the username
   * @return the user DTO
   * @throws ResourceNotFoundException if user not found
   */
  public UserDTO getUserDto(String username) {
    User user = userRepository
      .findByUsername(username)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND)
      );
    return new UserDTO(
      user.getId(),
      user.getUsername(),
      user.getEmail(),
      List.of(user.getRole())
    );
  }

  public UserDTO getUserDtoByEmail(String email) {
    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND)
      );
    return new UserDTO(
      user.getId(),
      user.getUsername(),
      user.getEmail(),
      List.of(user.getRole())
    );
  }
}
