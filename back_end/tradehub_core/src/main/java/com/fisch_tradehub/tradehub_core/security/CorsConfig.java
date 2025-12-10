package com.fisch_tradehub.tradehub_core.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(
      List.of("http://localhost:5173", "http://127.0.0.1:5173")
    ); // địa chỉ Vue dev
    cfg.setAllowedMethods(
      List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
    );
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(true); // cho phép gửi cookie

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
