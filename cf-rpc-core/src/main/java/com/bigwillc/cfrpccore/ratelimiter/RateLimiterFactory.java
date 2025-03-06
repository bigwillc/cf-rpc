package com.bigwillc.cfrpccore.ratelimiter;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and managing rate limiters
 *
 * @author bigwillc on 2025/3/6
 */
public class RateLimiterFactory {

    private final Map<String, RateLimiter> rateLimiters = new HashMap<>();

    // Default rate limiter implementation
    private final RateLimiter defaultRateLimiter = new TokenBucketRateLimiter();

    /**
     * Get or create a rate limiter for a service
     * @param service The service name
     * @return The rate limiter
     */
    public RateLimiter getRateLimiter(String service) {
        return rateLimiters.computeIfAbsent(service, k -> defaultRateLimiter);
    }

    /**
     * Initialize rate limiting for a service
     * @param service The service name
     * @param enabled Whether rate limiting is enabled
     * @param maxRequestsPerSecond Maximum requests per second
     */
    public void initializeRateLimiter(String service, boolean enabled, double maxRequestsPerSecond) {
        RateLimiter limiter = getRateLimiter(service);
        if (enabled) {
            limiter.initializeService(service, maxRequestsPerSecond);
        } else {
            limiter.initializeService(service, -1); // Disable rate limiting
        }
    }
}
