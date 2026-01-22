package com.venkat.rate_limiter.controller;

import com.venkat.rate_limiter.model.RateLimitRequest;
import com.venkat.rate_limiter.service.TokenBucketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RateLimitController {

    private final TokenBucketService tokenService;

    @Value("${app.rate-limit.capacity}")
    private int limitCapacity;

    public RateLimitController(TokenBucketService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/rate-limit")
    public ResponseEntity<Map<String, Object>> checkRateLimit(@RequestBody RateLimitRequest request) {
        // Ask the service if allowed
        Map<String, Object> result = tokenService.allowRequest(request.getClient_id());
        boolean allowed = (boolean) result.get("allowed");

        // Prepare Response Headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Limit", String.valueOf(limitCapacity));
        headers.add("X-RateLimit-Remaining", String.valueOf(result.get("remaining")));
        headers.add("X-RateLimit-Reset", String.valueOf(result.get("reset_at")));

        // Prepare JSON Body
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("client_id", request.getClient_id());

        if (allowed) {
            responseBody.put("status", "allowed");
            responseBody.put("remaining", result.get("remaining"));
            responseBody.put("reset_at", result.get("reset_at")); // Fixed key name
            return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
        } else {
            responseBody.put("status", "denied");
            responseBody.put("retry_after_seconds", result.get("retry_after"));
            headers.add("Retry-After", String.valueOf(result.get("retry_after")));
            return new ResponseEntity<>(responseBody, headers, HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    // Health check for Docker
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"healthy\"}");
    }
}