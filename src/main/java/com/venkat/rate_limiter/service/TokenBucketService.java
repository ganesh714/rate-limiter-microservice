package com.venkat.rate_limiter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBucketService {

    private final StringRedisTemplate redisTemplate;

    // Read values from application.properties
    @Value("${app.rate-limit.capacity}")
    private int capacity;

    @Value("${app.rate-limit.period-seconds}")
    private int periodSeconds;

    public TokenBucketService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Object> allowRequest(String clientId) {
        String key = "rate_limit:" + clientId;
        long currentTime = Instant.now().getEpochSecond();
        double refillRate = (double) capacity / periodSeconds;

        // 1. Get current data from Redis
        String tokensStr = (String) redisTemplate.opsForHash().get(key, "tokens");
        String lastRefillStr = (String) redisTemplate.opsForHash().get(key, "last_refill");

        double tokens = tokensStr == null ? capacity : Double.parseDouble(tokensStr);
        long lastRefill = lastRefillStr == null ? currentTime : Long.parseLong(lastRefillStr);

        // 2. Refill the bucket based on time passed
        long timeElapsed = currentTime - lastRefill;
        double newTokens = timeElapsed * refillRate;
        tokens = Math.min(capacity, tokens + newTokens);

        // 3. Check if we can allow the request
        boolean allowed = false;
        if (tokens >= 1.0) {
            tokens -= 1.0;
            allowed = true;
        }

        // 4. Calculate when the bucket will be full again (Reset Time)
        double timeToFull = (capacity - tokens) / refillRate;
        long resetAt = currentTime + (long) timeToFull;

        // 5. Save new values to Redis
        Map<String, String> updateMap = new HashMap<>();
        updateMap.put("tokens", String.valueOf(tokens));
        updateMap.put("last_refill", String.valueOf(currentTime));

        redisTemplate.opsForHash().putAll(key, updateMap);
        redisTemplate.expire(key, periodSeconds * 2, TimeUnit.SECONDS);

        // 6. Return the result
        Map<String, Object> result = new HashMap<>();
        result.put("allowed", allowed);
        result.put("remaining", (int) tokens);
        result.put("reset_at", resetAt);

        if (!allowed) {
            double retryAfter = (1.0 - tokens) / refillRate;
            result.put("retry_after", (int) Math.max(1, retryAfter));
        }

        return result;
    }
}
