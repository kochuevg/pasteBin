package api.ipa.service;

import api.ipa.dto.PastePreview;

import java.util.List;

public interface RedisService {
    List<PastePreview> getPublicFeed();
    void recordUniqueView(String storageKey, String ip, String agent);
}
