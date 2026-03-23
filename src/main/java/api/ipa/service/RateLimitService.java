package api.ipa.service;

import io.github.bucket4j.Bucket;

public interface RateLimitService {
    public Bucket resolveBucket(String key);
}
