package com.dp.awsldc;

import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  @GetMapping("/me")
  public Map<String, String> getMe(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    String friendlyName = jwt.getClaimAsString("cognito:username");
    return Map.of("username", friendlyName);
  }
}
