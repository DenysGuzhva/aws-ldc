package com.dp.awsldc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloAWS {

  @GetMapping("/")
  public String getHostIp() {
    try {
      return "Server IP Address: " + InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return "Error retrieving IP: " + e.getMessage();
    }
  }

}
