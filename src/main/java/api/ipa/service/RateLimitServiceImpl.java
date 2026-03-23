package api.ipa.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService{
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${app.limits.pastes.per-hour}")
    private int pastesPerHour;

    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {

        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(pastesPerHour)
                        .refillGreedy(pastesPerHour, Duration.ofHours(1))
                )
                .build();
    }
}
