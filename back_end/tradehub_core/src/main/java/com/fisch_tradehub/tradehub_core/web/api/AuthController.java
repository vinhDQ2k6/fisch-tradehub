package com.fisch_tradehub.tradehub_core.web.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fisch_tradehub.tradehub_core.service.AuthService;
import com.fisch_tradehub.tradehub_core.web.dto.LoginRequest;
import com.fisch_tradehub.tradehub_core.web.dto.RegisterRequest;
import com.fisch_tradehub.tradehub_core.web.dto.UserDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for authentication operations.
 * Handles user registration, login, logout, and session management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AuthService authService;

    /**
     * Authenticate user and create session.
     * 
     * @param request login credentials
     * @param httpRequest HTTP request
     * @param httpResponse HTTP response
     * @return authenticated user details
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password());

        Authentication authentication = authenticationManager.authenticate(authToken);

        // Create and save SecurityContext to generate session + JSESSIONID cookie
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        UserDTO dto = authService.getUserDto(authentication.getName());

        return ResponseEntity.ok(dto);
    }

    /**
     * Register a new user account.
     * 
     * @param request registration details
     * @return created user details
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO dto = authService.register(request);
        return ResponseEntity
                .created(URI.create("/api/auth/users/" + dto.id()))
                .body(dto);
    }

    /**
     * Get current authenticated user details.
     * 
     * @param principal authenticated user
     * @return current user details or 401 if not authenticated
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }
        return ResponseEntity.ok(authService.getUserDto(principal.getUsername()));
    }

    /**
     * Logout and invalidate current session.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param authentication current authentication
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {

        Authentication currentAuth = authentication;
        if (currentAuth == null) {
            currentAuth = SecurityContextHolder.getContext().getAuthentication();
        }

        CookieClearingLogoutHandler cookieClearingLogoutHandler =
                new CookieClearingLogoutHandler("JSESSIONID", "remember-me");

        cookieClearingLogoutHandler.logout(request, response, currentAuth);

        if (currentAuth != null) {
            new SecurityContextLogoutHandler().logout(request, response, currentAuth);
        } else {
            SecurityContextHolder.clearContext();
        }

        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        SecurityContextHolder.setContext(emptyContext);
        securityContextRepository.saveContext(emptyContext, request, response);
        return ResponseEntity.ok("Logged out");
    }
}