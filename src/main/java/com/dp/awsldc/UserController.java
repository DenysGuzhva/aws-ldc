package com.dp.awsldc;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  @GetMapping("/me")
  public Map<String, String> getMe(Authentication authentication) {
    return Map.of("username", authentication.getName());
  }
}
