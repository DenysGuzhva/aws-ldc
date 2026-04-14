package com.dp.awsldc;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  @GetMapping("/me")
  public String getUsername(Principal principal) {
    if (principal == null) return "Unknown User";
    return principal.getName();
  }
}
