package api.ipa.dto;

import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.ExpirationFormat;
import api.ipa.entity.helpEntity.PasteFormat;
import api.ipa.entity.helpEntity.PasteVisibility;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record PasteRequest(
        String title,
        PasteFormat format,
        PasteVisibility visibility,
        Boolean deleteAfterExpiration,
        String data,
        ExpirationFormat expirationDate
){
    public PasteRequest{
        if(format == null) format = PasteFormat.PLAIN_TEXT;
        if(visibility == null) visibility = PasteVisibility.PUBLIC;
    }

    public Paste toPaste(String s3Key, User creator) {
        Paste paste = new Paste();
        paste.setCreationDate(Instant.now());
        paste.setExpirationDate(expirationDate.calculateExpirationTime());
        paste.setPasteFormat(this.format());
        paste.setVisibility(this.visibility());
        paste.setCreator(creator);
        paste.setDeleteAfterExpiration(this.deleteAfterExpiration());
        paste.setTitle(this.title());
        paste.setStorageKey(s3Key);
        return paste;
    }
}
