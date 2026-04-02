package com.dp.awsldc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestController
public class HelloAWS {

  private static final Logger log = LoggerFactory.getLogger(HelloAWS.class);
  private static final String METADATA_BASE_URL = "http://169.254.169.254/latest/";
  private final String bucketName = "ldc-bucket-064963113358-us-east-1-an";

  private final S3Client s3Client = S3Client.builder()
      .region(Region.US_EAST_1)
      .build();

  private final String cloudFrontDomain = "d98njmkt2dxwf.cloudfront.net";

  @PostMapping("/upload")
  public Map<String, String> uploadImage(@RequestParam("image") MultipartFile file, @RequestParam("name") String customName) {
    try {
      // 1. Clean the base name (remove .jpg if provided)
      String baseName = customName.toLowerCase().endsWith(".jpg")
          ? customName.substring(0, customName.length() - 4)
          : customName;

      // 2. Logic to find the next version (v1, v2, etc.)
      int version = 1;
      boolean exists = true;
      String finalFileName = "";

      // Check S3 for existing files starting with this name to determine the version
      while (exists) {
        finalFileName = baseName + "_v" + version + ".jpg";
        try {
          s3Client.headObject(HeadObjectRequest.builder()
              .bucket(bucketName)
              .key(finalFileName)
              .build());
          version++; // If no error, file exists, try next version
        } catch (S3Exception e) {
          if (e.statusCode() == 404) {
            exists = false; // File doesn't exist, we found our version!
          } else {
            throw e;
          }
        }
      }

      // 3. Upload the NEW version
      byte[] bytes = file.getBytes();
      s3Client.putObject(PutObjectRequest.builder()
              .bucket(bucketName)
              .key(finalFileName)
              .contentType("image/jpeg")
              .build(),
          RequestBody.fromBytes(bytes));

      // 4. Return the NEW URL (CloudFront sees this as a new file instantly)
      String finalUrl = "https://" + cloudFrontDomain + "/" + finalFileName;

      String thumbUrl = "https://" + cloudFrontDomain + "/thumbnails/" + finalFileName;

      return Map.of(
          "message", "Uploaded as new version: " + "v" + version,
          "url", finalUrl,
          "thumbnailUrl", thumbUrl,
          "status", "Success"
      );
    } catch (Exception e) {
      log.error("Upload failed", e);
      return Map.of("error", e.getMessage());
    }
  }

  @GetMapping("/")
  public Map<String, String> getInstanceInfo() {
    String token = getMetadataToken();
    String hostIp = fetchMetadata(token, "meta-data/local-ipv4");
    String az = fetchMetadata(token, "meta-data/placement/availability-zone");
    String containerId = "Unknown";
    try {
      containerId = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      log.error("Could not get hostname");
    }
    return Map.of("hostIp", hostIp, "az", az, "containerId", containerId);
  }

  private String getMetadataToken() {
    try {
      URL url = new URL(METADATA_BASE_URL + "api/token");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("PUT");
      con.setRequestProperty("X-aws-ec2-metadata-token-ttl-seconds", "21600");
      con.setConnectTimeout(2000);
      try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
        return in.readLine();
      }
    } catch (Exception e) {
      return null;
    }
  }

  private String fetchMetadata(String token, String path) {
    if (token == null) return "Unknown";
    try {
      URL url = new URL(METADATA_BASE_URL + path);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("X-aws-ec2-metadata-token", token);
      try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) { content.append(line); }
        return content.toString();
      }
    } catch (Exception e) {
      return "Error";
    }
  }
}