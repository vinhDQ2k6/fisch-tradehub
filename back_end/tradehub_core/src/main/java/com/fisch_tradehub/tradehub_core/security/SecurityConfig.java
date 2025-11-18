package com.fisch_tradehub.tradehub_core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final UserDetailsService userDetailsService;

    SecurityConfig(CorsConfig corsConfig, UserDetailsService userDetailsService) {
        this.corsConfig = corsConfig;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/auth/**"))
                .cors(cors -> {
                    cors.configurationSource(corsConfig.corsConfigurationSource());
                })

                .authorizeHttpRequests(auth -> auth
                        // cho phép các request không cần đăng nhập
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // các request còn lại phải đăng nhập
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login") // where the form is POSTed
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(200); // cookies are set; frontend can call /api/auth/me for user data
                        })
                        .failureHandler((request, response, ex) -> {
                            response.setStatus(401);
                        }))

                .rememberMe(save -> save
                        .key("IamYourAdminYouKnow?")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(24 * 60 * 60)
                        .userDetailsService(userDetailsService));

        return http.build();
    }

    // Nếu cần dùng AuthenticationManager trong AuthController (tự gọi authenticate)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}