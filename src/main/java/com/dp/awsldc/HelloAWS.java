package com.dp.awsldc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloAWS {

  @GetMapping("/")
  public String getHostIp() {
    // Look for the Environment Variable first
    String serverName = System.getenv("SERVER_NAME");

    if (serverName != null && !serverName.isEmpty()) {
      return "Connected to: " + serverName;
    }

    // Fallback to the IP address if the variable isn't set
    try {
      return "Server IP Address: " + InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return "Error: " + e.getMessage();
    }
  }

}
