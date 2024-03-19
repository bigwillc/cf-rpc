package com.bigwillc.cfrpccore.registry;

/**
 * @author bigwillc on 2024/3/19
 */
public interface ChangeedListener {
    void fire(Event event);
}
