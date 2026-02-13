package api.ipa.service;

import api.ipa.dto.PasteRequest;
import api.ipa.entity.Paste;
import api.ipa.entity.User;
import org.springframework.stereotype.Service;

public interface PasteService {
    Paste createPaste(PasteRequest newPaste, String s3Key, User creator);
    
}
