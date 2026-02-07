package com.example.auction.freqcap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class FrequencyCapService {

    private final StringRedisTemplate redis;

    // Bulkhead: limit concurrent Redis operations
    private final Semaphore redisSemaphore;

    // Metrics
    private final Counter redisRejectedCounter;

    // For Milestone A2/A3: simple cap
    private static final int CAP_PER_DAY = 5;
    private static final Duration TTL = Duration.ofDays(1);

    // Fail-fast behavior
    private static final long REDIS_ACQUIRE_TIMEOUT_MS = 10;

    public FrequencyCapService(StringRedisTemplate redis, MeterRegistry meterRegistry) {
        this.redis = redis;

        // Tune numbers later after load test. Start conservative.
        this.redisSemaphore = new Semaphore(50);

        this.redisRejectedCounter = meterRegistry.counter("bulkhead.rejected", "dependency", "redis");
    }

    public boolean canServe(String userId, long campaignId) {
        if (!tryAcquire(redisSemaphore, REDIS_ACQUIRE_TIMEOUT_MS)) {
            redisRejectedCounter.increment();
            // If Redis is saturated/slow, we choose safety:
            // returning true here means "do not block auction due to cap checks".
            // In real AdTech you might choose "fail closed" depending on policy.
            return true;
        }

        try {
            String key = key(userId, campaignId);
            String val = redis.opsForValue().get(key);
            if (val == null) return true;

            int count;
            try {
                count = Integer.parseInt(val);
            } catch (NumberFormatException e) {
                return true;
            }
            return count < CAP_PER_DAY;
        } finally {
            redisSemaphore.release();
        }
    }

    public void recordServe(String userId, long campaignId) {
        if (!tryAcquire(redisSemaphore, REDIS_ACQUIRE_TIMEOUT_MS)) {
            redisRejectedCounter.increment();
            // If overloaded, skip recording rather than block requests.
            // (In real system you'd handle via async event pipeline.)
            return;
        }

        try {
            String key = key(userId, campaignId);
            Long newValue = redis.opsForValue().increment(key);
            if (newValue != null && newValue == 1L) {
                redis.expire(key, TTL);
            }
        } finally {
            redisSemaphore.release();
        }
    }

    private String key(String userId, long campaignId) {
        return "freqcap:user:" + userId + ":cmp:" + campaignId;
    }

    private static boolean tryAcquire(Semaphore sem, long timeoutMs) {
        try {
            return sem.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
