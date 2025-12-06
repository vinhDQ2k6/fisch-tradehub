package com.fisch_tradehub.tradehub_core.security;

import com.fisch_tradehub.tradehub_core.entity.User;
import com.fisch_tradehub.tradehub_core.repository.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest)
    throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String email = oAuth2User.getAttribute("email");

    if (email == null) {
      throw new OAuth2AuthenticationException(
        "Email not found from OAuth2 provider"
      );
    }

    User user = userRepository
      .findByEmail(email)
      .orElseGet(() -> {
        // Create new user
        String username = email.substring(0, email.indexOf("@"));
        // Handle potential duplicate usernames if necessary in the future

        User newUser = User.builder()
          .email(email)
          .username(username)
          .password("") // No password for OAuth users
          .role("ROLE_USER")
          .active(true)
          .build();
        return userRepository.save(newUser);
      });

    return new DefaultOAuth2User(
      Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
      oAuth2User.getAttributes(),
      "email"
    );
  }
}
