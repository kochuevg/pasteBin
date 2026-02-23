package api.ipa.dto;


import api.ipa.entity.Paste;
import api.ipa.entity.helpEntity.PasteVisibility;

import java.time.Instant;

public record PasteResponse(
        String key,
        String title,
        String data,
        String format,
        Instant createdAt,
        Instant expireAt,
        PasteVisibility visibility,
        long views
) {
    public static PasteResponse from(Paste paste, String content) {
        return new PasteResponse(
                paste.getStorageKey(),
                paste.getTitle(),
                content,
                paste.getPasteFormat().name(),
                paste.getCreationDate(),
                paste.getExpirationDate(),
                paste.getVisibility(),
                paste.getViews()
        );
    }
}
