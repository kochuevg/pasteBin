package api.ipa.service;

import api.ipa.dto.PastePreview;
import api.ipa.dto.PasteResponse;
import api.ipa.entity.Paste;

import java.util.List;

public interface RedisService {
    List<PastePreview> getPublicFeed();
    void recordUniqueView(String storageKey, String ip, String agent);
    PasteResponse getPasteFromCache(String storageKey);
    void savePasteToCache(PasteResponse paste);
}
