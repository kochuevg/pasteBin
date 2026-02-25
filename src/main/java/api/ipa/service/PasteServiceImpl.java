package api.ipa.service;

import api.ipa.dto.PasteRequest;
import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.repository.PasteRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class PasteServiceImpl implements PasteService{

    private final PasteRepository pasteRepository;

    @Override
    public Paste createPaste(PasteRequest newPaste, String s3Key, User creator) {
        Paste paste = new Paste();
        Instant time = Instant.now();
        paste.setCreationDate(time);
        paste.setExpirationDate(time.plus(newPaste.expirationDate(), ChronoUnit.DAYS));
        paste.setPasteFormat(newPaste.format());
        paste.setVisibility(newPaste.visibility());
        paste.setCreator(creator);
        paste.setDeleteAfterExpiration(newPaste.deleteAfterExpiration());
        paste.setTitle(newPaste.title());
        paste.setStorageKey(s3Key);
        return paste;
    }

    @Override
    public Paste save(Paste paste) {
        return pasteRepository.save(paste);
    }

    @Override
    public Optional<Paste> findPasteByStorageKey(String key) {
        return pasteRepository.findByStorageKey(key);
    }

    @Override
    public void deletePaste(Paste paste){
        pasteRepository.delete(paste);
    }
}
