package com.fisch_tradehub.tradehub_core.web.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fisch_tradehub.tradehub_core.service.AdminUserService;
import com.fisch_tradehub.tradehub_core.web.dto.AdminUserDTO;
import com.fisch_tradehub.tradehub_core.web.dto.UpdateUserRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for admin user management operations.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final AdminUserService adminUserService;

    /**
     * Get all users in the system.
     *
     * @return list of all users
     */
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    /**
     * Get a specific user by ID.
     *
     * @param id the user ID
     * @return the user details
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    /**
     * Update user role and/or active status.
     *
     * @param id the user ID
     * @param request the update request
     * @param userDetails the authenticated admin user
     * @return the updated user details
     */
    @PutMapping("/{id}")
    public ResponseEntity<AdminUserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            adminUserService.updateUser(id, request, userDetails.getUsername())
        );
    }

    /**
     * Soft delete a user (set active=false).
     *
     * @param id the user ID
     * @param userDetails the authenticated admin user
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminUserService.softDeleteUser(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
