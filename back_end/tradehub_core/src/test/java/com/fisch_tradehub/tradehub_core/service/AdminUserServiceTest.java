package com.fisch_tradehub.tradehub_core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.exception.BusinessException;
import com.fisch_tradehub.tradehub_core.exception.ResourceNotFoundException;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import com.fisch_tradehub.tradehub_core.web.dto.AdminUserDTO;
import com.fisch_tradehub.tradehub_core.web.dto.UpdateUserRequest;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    // ========== Test Data Helpers ==========

    private User createTestUser(Long id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    // ========== getAllUsers Tests ==========

    @Test
    @DisplayName("getAllUsers - Should return all users")
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = List.of(
            createTestUser(1L, "admin", "ROLE_ADMIN"),
            createTestUser(2L, "user1", "ROLE_USER")
        );
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<AdminUserDTO> result = adminUserService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).username()).isEqualTo("admin");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers - Should return empty list when no users")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<AdminUserDTO> result = adminUserService.getAllUsers();

        assertThat(result).isEmpty();
    }

    // ========== getUserById Tests ==========

    @Test
    @DisplayName("getUserById - Should return user when exists")
    void getUserById_ShouldReturnUser_WhenExists() {
        User user = createTestUser(1L, "admin", "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserDTO result = adminUserService.getUserById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("admin");
    }

    @Test
    @DisplayName("getUserById - Should throw when user not found")
    void getUserById_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    // ========== updateUser Tests ==========

    @Test
    @DisplayName("updateUser - Should update role successfully")
    void updateUser_ShouldUpdateRole_WhenValid() {
        User user = createTestUser(1L, "user1", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateUserRequest request = new UpdateUserRequest("ROLE_STAFF", null);
        AdminUserDTO result = adminUserService.updateUser(1L, request, "admin");

        assertThat(result.role()).isEqualTo("ROLE_STAFF");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Should throw when admin disables self")
    void updateUser_ShouldThrow_WhenAdminDisablesSelf() {
        User adminUser = createTestUser(1L, "admin", "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        UpdateUserRequest request = new UpdateUserRequest("ROLE_ADMIN", false);

        assertThatThrownBy(() ->
            adminUserService.updateUser(1L, request, "admin"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot disable your own account");
    }

    @Test
    @DisplayName("updateUser - Should throw when invalid role")
    void updateUser_ShouldThrow_WhenInvalidRole() {
        User user = createTestUser(1L, "user1", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateUserRequest request = new UpdateUserRequest("ROLE_INVALID", null);

        assertThatThrownBy(() ->
            adminUserService.updateUser(1L, request, "admin"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Invalid role");
    }

    @Test
    @DisplayName("updateUser - Should update only active status when role is null")
    void updateUser_ShouldUpdateOnlyActiveStatus_WhenRoleIsNull() {
        User user = createTestUser(1L, "user1", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateUserRequest request = new UpdateUserRequest(null, false);
        AdminUserDTO result = adminUserService.updateUser(1L, request, "admin");

        assertThat(result.role()).isEqualTo("ROLE_USER"); // unchanged
        assertThat(result.active()).isFalse();
        verify(userRepository).save(any(User.class));
    }

    // ========== softDeleteUser Tests ==========

    @Test
    @DisplayName("softDeleteUser - Should set active false")
    void softDeleteUser_ShouldSetActiveFalse() {
        User user = createTestUser(2L, "user1", "ROLE_USER");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        adminUserService.softDeleteUser(2L, "admin");

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("softDeleteUser - Should throw when deleting self")
    void softDeleteUser_ShouldThrow_WhenDeletingSelf() {
        User adminUser = createTestUser(1L, "admin", "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() ->
            adminUserService.softDeleteUser(1L, "admin"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot disable your own account");
    }
}
