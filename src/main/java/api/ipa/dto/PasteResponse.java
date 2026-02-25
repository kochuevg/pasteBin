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
        long views,
        String ownerUsername
) {
    public static PasteResponse from(Paste paste, String content, String ownerUsername) {
        return new PasteResponse(
                paste.getStorageKey(),
                paste.getTitle(),
                content,
                paste.getPasteFormat().name(),
                paste.getCreationDate(),
                paste.getExpirationDate(),
                paste.getVisibility(),
                paste.getViews(),
                ownerUsername
        );
    }

    public PasteResponse withAddedViews(Long newViews){
        return new PasteResponse(
                this.key(),
                this.title(),
                this.data,
                this.format(),
                this.createdAt,
                this.expireAt,
                this.visibility,
                this.views() + newViews,
                this.ownerUsername
        );
    }
}
