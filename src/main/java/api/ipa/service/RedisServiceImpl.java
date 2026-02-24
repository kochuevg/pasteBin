package api.ipa.service;

import api.ipa.dto.PastePreview;
import api.ipa.dto.PasteResponse;
import api.ipa.entity.Paste;
import api.ipa.entity.helpEntity.PasteVisibility;
import api.ipa.exception.PasteNotFoundException;
import api.ipa.repository.PasteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final PasteRepository pasteRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String FEED_CACHE_KEY = "feed:public:latest";
    private static final String VIEW_KEY_PREFIX = "views:unique:paste:";
    private static final String PASTE_CACHE_KEY = "paste:details:";

    @Override
    @SuppressWarnings("unchecked")
    public List<PastePreview> getPublicFeed() {
        List<PastePreview> cachedFeed = (List<PastePreview>) redisTemplate.opsForValue().get(FEED_CACHE_KEY);
        log.debug("Found cachedFeed size is: {}", cachedFeed == null ? 0 : cachedFeed.size());
        if(cachedFeed == null || cachedFeed.isEmpty()){
            renewFeedCache();
            cachedFeed = (List<PastePreview>) redisTemplate.opsForValue().get(FEED_CACHE_KEY);
        }
        return cachedFeed;
    }

    @Override
    public void recordUniqueView(String storageKey, String ip, String agent) {
        String rawFingerprint = ip + "|" + agent;
        String hashedFingerprint = DigestUtils.md5DigestAsHex(rawFingerprint.getBytes());

        redisTemplate.opsForHyperLogLog().add(VIEW_KEY_PREFIX + storageKey, hashedFingerprint);
    }

    @Override
    public PasteResponse getPasteFromCache(String storageKey) {
        return (PasteResponse) redisTemplate.opsForValue().get(PASTE_CACHE_KEY + storageKey);
    }

    @Override
    public void savePasteToCache(PasteResponse paste) {
        if (paste.expireAt() != null) {
            Duration timeUntilExpiration = Duration.between(Instant.now(), paste.expireAt());
            redisTemplate.opsForValue().set(PASTE_CACHE_KEY + paste.key(), paste, timeUntilExpiration);
        } else {
            redisTemplate.opsForValue().set(PASTE_CACHE_KEY + paste.key(), paste);
        }
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void synchronizeCacheAndDatabase() {
        log.info("Starting Redis-to-PostgreSQL background synchronization...");

        flushViewsToDatabase();
        renewFeedCache();

        log.info("Background synchronization complete.");
    }

    private void renewFeedCache() {
        List<Paste> latestPastes = pasteRepository.findByVisibilityOrderByCreationDate(
                PasteVisibility.PUBLIC,
                PageRequest.of(0, 50)
        );

        List<PastePreview> feedDtoList = latestPastes.stream()
                .map(PastePreview::toPastePreview)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(FEED_CACHE_KEY, feedDtoList);
        log.debug("Renewed global feed cache with {} items.", feedDtoList.size());
    }

    private void flushViewsToDatabase() {
        Set<String> keys = redisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        int updatedCount = 0;

        for (String key : keys) {
            String storageKey = key.replace(VIEW_KEY_PREFIX, "");

            Long uniqueViews = redisTemplate.opsForHyperLogLog().size(key);

            if (uniqueViews != null && uniqueViews > 0) {
                pasteRepository.addViewsToPaste(storageKey, uniqueViews);

                patchCachedPasteViews(storageKey, uniqueViews);

                updatedCount++;
            }

            redisTemplate.delete(key);
        }

        log.debug("Flushed new unique views to PostgreSQL for {} pastes.", updatedCount);
    }

    private void patchCachedPasteViews(String storageKey, long addedViews) {
        String cacheKey = PASTE_CACHE_KEY + storageKey;

        PasteResponse cachedPaste = (PasteResponse) redisTemplate.opsForValue().get(cacheKey);

        if (cachedPaste != null) {
            PasteResponse updatedPaste = cachedPaste.withAddedViews(addedViews);

            Long timeToLiveSeconds = redisTemplate.getExpire(cacheKey);

            if (timeToLiveSeconds != null && timeToLiveSeconds > 0) {
                redisTemplate.opsForValue().set(cacheKey, updatedPaste, Duration.ofSeconds(timeToLiveSeconds));
            } else {
                redisTemplate.opsForValue().set(cacheKey, updatedPaste);
            }
        }
    }
}
