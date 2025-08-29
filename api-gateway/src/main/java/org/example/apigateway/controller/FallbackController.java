package org.example.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> usersFallback() {
        Map<String, Object> response = Map.of(
                "message", "User Service is temporarily unavailable. Please try again later.",
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "timestamp", Instant.now().toString(),
                "service", "user-service"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        Map<String, Object> response = Map.of(
                "message", "Authentication Service is temporarily unavailable. Please try again later.",
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "timestamp", Instant.now().toString(),
                "service", "auth-service"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> filesFallback() {
        Map<String, Object> response = Map.of(
                "message", "File Service is temporarily unavailable. Please try again later.",
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "timestamp", Instant.now().toString(),
                "service", "file-service"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profileFallback() {
        Map<String, Object> response = Map.of(
                "message", "Profile Service is temporarily unavailable. Please try again later.",
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "timestamp", Instant.now().toString(),
                "service", "profile-service"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

}