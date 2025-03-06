package com.bigwillc.cfrpccore.ratelimiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author bigwillc on 2025/3/5
 */
public interface RateLimiter {

    /**
     * Try to acquire a permit
     * @param service The service name
     * @return true if permitted, false if rate limit exceeded
     */
    boolean tryAcquire(String service);

    /**
     * Check if rate limiting is enabled for a service
     * @param service The service name
     * @return true if enabled, false otherwise
     */
    boolean isEnabled(String service);

    /**
     * Initialize rate limiter for a service
     * @param service The service name
     * @param maxRequestsPerSecond Maximum requests per second
     */
    void initializeService(String service, double maxRequestsPerSecond);
}
