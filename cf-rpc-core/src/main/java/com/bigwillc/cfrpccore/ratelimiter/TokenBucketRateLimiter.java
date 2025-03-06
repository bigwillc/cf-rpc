package com.bigwillc.cfrpccore.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom token bucket rate limiter implementation
 *
 * @author bigwillc on 2025/3/6
 */
public class TokenBucketRateLimiter implements RateLimiter{

    private static class TokenBucket {
        private final double refillRate;        // Tokens per millisecond
        private final double capacity;          // Maximum tokens
        private double availableTokens;         // Current tokens
        private long lastRefillTimestamp;       // Last refill time in milliseconds

        public TokenBucket(double maxRequestsPerSecond) {
            this.refillRate = maxRequestsPerSecond / 1000.0; // Convert to tokens per millisecond
            this.capacity = maxRequestsPerSecond;            // Maximum burst size
            this.availableTokens = maxRequestsPerSecond;     // Start with full capacity
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean tryAcquire() {
            refill();

            if (availableTokens >= 1.0) {
                availableTokens -= 1.0;
                return true;
            }

            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timeElapsed = now - lastRefillTimestamp;

            if (timeElapsed > 0) {
                // Calculate tokens to add based on time elapsed
                double tokensToAdd = timeElapsed * refillRate;
                availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
                lastRefillTimestamp = now;
            }
        }
    }

    private final Map<String, TokenBucket> rateLimiters = new ConcurrentHashMap<>();
    private final Map<String, Boolean> enabledServices = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String service) {
        if (!isEnabled(service)) {
            return true; // If not enabled, always allow
        }

        TokenBucket bucket = rateLimiters.get(service);
        if (bucket == null) {
            return true; // If no limiter configured, allow
        }

        return bucket.tryAcquire();
    }

    @Override
    public boolean isEnabled(String service) {
        return enabledServices.getOrDefault(service, false);
    }

    @Override
    public void initializeService(String service, double maxRequestsPerSecond) {
        if (maxRequestsPerSecond <= 0) {
            enabledServices.put(service, false);
            rateLimiters.remove(service);
        } else {
            enabledServices.put(service, true);
            rateLimiters.put(service, new TokenBucket(maxRequestsPerSecond));
        }
    }
}
