package api.ipa.service;

import api.ipa.dto.PasteRequest;
import api.ipa.entity.Paste;
import api.ipa.entity.User;

import java.util.Optional;

public interface PasteService {
    Paste save(Paste paste);
    Optional<Paste> findPasteByStorageKey(String key);
    void deletePaste(Paste paste);
}
