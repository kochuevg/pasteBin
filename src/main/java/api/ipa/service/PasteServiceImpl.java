package api.ipa.service;

import api.ipa.dto.PasteRequest;
import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.repository.PasteRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class PasteServiceImpl implements PasteService{

    private final PasteRepository pasteRepository;

    @Override
    public Paste createPaste(PasteRequest newPaste, String s3Key, User creator) {
        Paste paste = new Paste();
        paste.setPasteFormat(newPaste.format());
        paste.setVisibility(newPaste.visibility());
        paste.setUser(creator);
        paste.setDeleteAfterExpiration(newPaste.deleteAfterExpiration());
        paste.setTitle(newPaste.title());
        paste.setS3Key(s3Key);
        paste.setExpirationDate(newPaste.expirationDate());
        return pasteRepository.save(paste);
    }
}
