package com.bigwillc.cfrpccore.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CFProvider {
    /**
     * Enable rate limiting for this provider
     * Default is false (disabled)
     */
    boolean rateLimit() default false;

    /**
     * Maximum number of requests allowed per second
     * Only effective when rateLimit is true
     */
    double maxRequestsPerSecond() default 100.0;
}
