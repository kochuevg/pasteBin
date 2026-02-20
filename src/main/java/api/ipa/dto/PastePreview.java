package api.ipa.dto;

import api.ipa.entity.Paste;
import api.ipa.entity.helpEntity.PasteFormat;
import api.ipa.entity.helpEntity.PasteVisibility;

import java.time.Instant;

public record PastePreview(
        String title,
        Instant creationDate,
        Instant expirationDate,
        PasteVisibility pasteVisibility,
        PasteFormat pasteFormat,
        long views
) {
    public static PastePreview toPastePreview(Paste paste) {
        return new PastePreview(
                paste.getTitle(),
                paste.getCreationDate(),
                paste.getExpirationDate(),
                paste.getVisibility(),
                paste.getPasteFormat(),
                paste.getLogs().size()
        );
    }
}
