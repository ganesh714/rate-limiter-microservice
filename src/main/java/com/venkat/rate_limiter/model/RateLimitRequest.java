package com.venkat.rate_limiter.model;

import lombok.Data;

@Data
public class RateLimitRequest {
    private String client_id;
    private String request_id;
}