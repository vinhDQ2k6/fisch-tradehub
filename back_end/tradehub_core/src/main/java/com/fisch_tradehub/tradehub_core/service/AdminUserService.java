package com.fisch_tradehub.tradehub_core.service;

import com.fisch_tradehub.tradehub_core.common.Constants;
import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.exception.BusinessException;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.AdminUserDTO;
import com.fisch_tradehub.tradehub_core.web.dto.UpdateUserRequest;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for admin user management operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

  private final UserRepository userRepository;

  private static final Set<String> VALID_ROLES = Set.of(
      Constants.ROLE_USER,
      Constants.ROLE_STAFF,
      Constants.ROLE_ADMIN
  );

  /**
   * Get all users in the system.
   *
   * @return list of all users
   */
  public List<AdminUserDTO> getAllUsers() {
    return userRepository.findAll().stream()
        .map(AdminUserDTO::from)
        .toList();
  }

  /**
   * Get a specific user by ID.
   *
   * @param id the user ID
   * @return the user details
   * @throws ResourceNotFoundException if user not found
   */
  public AdminUserDTO getUserById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));
    return AdminUserDTO.from(user);
  }

  /**
   * Update user role and/or active status.
   *
   * @param id the user ID
   * @param request the update request
   * @param currentUsername the username of the admin performing the update
   * @return the updated user
   * @throws ResourceNotFoundException if user not found
   * @throws BusinessException if admin tries to disable themselves or invalid role
   */
  @Transactional
  public AdminUserDTO updateUser(Long id, UpdateUserRequest request, String currentUsername) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

    // Constraint: cannot disable self
    if (user.getUsername().equals(currentUsername) &&
        Boolean.FALSE.equals(request.active())) {
      throw new BusinessException(Constants.CANNOT_DISABLE_SELF);
    }

    // Constraint: role must be valid
    if (!VALID_ROLES.contains(request.role())) {
      throw new BusinessException(Constants.INVALID_ROLE);
    }

    user.setRole(request.role());
    if (request.active() != null) {
      user.setActive(request.active());
    }

    return AdminUserDTO.from(userRepository.save(user));
  }

  /**
   * Soft delete a user (set active=false).
   *
   * @param id the user ID
   * @param currentUsername the username of the admin performing the deletion
   * @throws ResourceNotFoundException if user not found
   * @throws BusinessException if admin tries to delete themselves
   */
  @Transactional
  public void softDeleteUser(Long id, String currentUsername) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(Constants.USER_NOT_FOUND));

    if (user.getUsername().equals(currentUsername)) {
      throw new BusinessException(Constants.CANNOT_DISABLE_SELF);
    }

    user.setActive(false);
    userRepository.save(user);
  }
}
