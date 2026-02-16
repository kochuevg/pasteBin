package api.ipa.dto;

import api.ipa.entity.helpEntity.PasteFormat;
import api.ipa.entity.helpEntity.PasteVisibility;

import java.time.Instant;

public record PasteRequest(
        String title,
        PasteFormat format,
        PasteVisibility visibility,
        Boolean deleteAfterExpiration,
        Long userId,
        String data,
        Long expirationDate
){
    public PasteRequest{
        if(format == null) format = PasteFormat.PLAIN_TEXT;
        if(visibility == null) visibility = PasteVisibility.PUBLIC;
    }
}
