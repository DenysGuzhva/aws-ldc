package com.dp.awsldc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/me")
  public UserEntity getCurrentUser(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    String sub = jwt.getSubject();

    return userRepository.findByCognitoSub(sub)
        .map(user -> {
          user.setLastLogin(LocalDateTime.now());
          return userRepository.save(user);
        })
        .orElseGet(() -> {
          UserEntity newUser = new UserEntity();
          newUser.setCognitoSub(sub);
          newUser.setEmail(jwt.getClaimAsString("email"));
          newUser.setLastLogin(LocalDateTime.now());

          String birthdateStr = jwt.getClaimAsString("birthdate");

          if (birthdateStr != null) {
            newUser.setBirthdate(LocalDate.parse(birthdateStr));
          }

          return userRepository.save(newUser);
        });
  }
}
