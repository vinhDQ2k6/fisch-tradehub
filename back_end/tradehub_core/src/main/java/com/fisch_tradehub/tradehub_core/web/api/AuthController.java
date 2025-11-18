package com.fisch_tradehub.tradehub_core.web.api;

import com.fisch_tradehub.tradehub_core.web.dto.LoginRequest;
import com.fisch_tradehub.tradehub_core.web.dto.RegisterRequest;
import com.fisch_tradehub.tradehub_core.web.dto.UserDTO;
import com.fisch_tradehub.tradehub_core.web.model.User;
import com.fisch_tradehub.tradehub_core.dao.UserRepository;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    );

            Authentication authentication = authenticationManager.authenticate(authToken);

            // Tạo và lưu SecurityContext -> sinh session + cookie JSESSIONID
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow();

            UserDTO dto = new UserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    List.of(user.getRole())
            );

            return ResponseEntity.ok(dto);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // 1. Kiểm tra trùng username/email
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.status(409).body("Username already exists");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) { // nếu bạn có findByEmail
            return ResponseEntity.status(409).body("Email already exists");
        }

        // 2. Tạo user mới
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); // BCrypt
        user.setRole("USER");       // trong DB lưu "USER", UserDetailsService sẽ .roles("USER")
                                  // nếu bạn muốn mặc định ADMIN cho dev thì đổi "ADMIN"
        user.setActive(true);       // hoặc setEnabled(true) tùy field của bạn

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // fallback nếu DB có unique constraint mà chưa check phía trên
            return ResponseEntity.status(409).body("User already exists");
        }

        UserDTO dto = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                List.of(user.getRole())
        );

        // 201 Created + location optional
        return ResponseEntity
                .created(URI.create("/api/auth/users/" + user.getId()))
                .body(dto);
    }

    // Dùng để Vue check xem đang login hay không
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(); // nếu đến đây thì gần như chắc chắn tồn tại

        UserDTO dto = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                List.of(user.getRole())
        );

        return ResponseEntity.ok(dto);
    }

    // Hủy session hiện tại + cookie JSESSIONID
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