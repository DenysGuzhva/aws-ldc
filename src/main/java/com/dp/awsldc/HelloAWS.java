package com.dp.awsldc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloAWS {

  private static final Logger log = LoggerFactory.getLogger(HelloAWS.class);

  @GetMapping("/")
  public Map<String, String> getInstanceInfo() {
    String ip = getLocalIp();
    String az = getAvailabilityZone();

    log.info("Request served from IP: {} in AZ: {}", ip, az);
    return Map.of("ip", ip, "az", az);
  }

  private String getLocalIp() {
    try {
      URL url = new URL("http://169.254.169.254/latest/meta-data/local-ipv4");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      BufferedReader in = new BufferedReader(
          new InputStreamReader(con.getInputStream())
      );

      String inputLine;
      StringBuilder content = new StringBuilder();

      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }

      in.close();
      return content.toString();

    } catch (Exception e) {
      return "Unable to fetch IP: " + e.getMessage();
    }
  }

  public String getAvailabilityZone() {
    try {
      URL url = new URL("http://169.254.169.254/latest/meta-data/placement/availability-zone");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");

      BufferedReader in = new BufferedReader(
          new InputStreamReader(con.getInputStream())
      );

      return in.readLine();

    } catch (Exception e) {
      return "Unknown AZ";
    }
  }

}
