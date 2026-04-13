package com.freelanceflow.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Expert-level Resilience Service: Handles distributed idempotency using Redis.
 * Ensures that duplicate requests (like Razorpay webhooks) are processed exactly once.
 */
@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Attempts to acquire an idempotency lock for a specific key.
     * @param key The unique key for the operation (e.g., razorpay_payment_id)
     * @param timeout The duration for which the lock should be held (TTL)
     * @return true if the lock was successfully acquired (meaning this is the first time we see this key)
     */
    public boolean acquire(String key, Duration timeout) {
        String fullKey = CacheConstants.IDEMPOTENCY + ":" + key;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(fullKey, "locked", timeout);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Releases the idempotency lock. Usually used if the operation fails and needs to be retried.
     */
    public void release(String key) {
        String fullKey = CacheConstants.IDEMPOTENCY + ":" + key;
        redisTemplate.delete(fullKey);
    }
}
