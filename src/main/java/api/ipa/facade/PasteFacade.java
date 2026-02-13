package api.ipa.facade;

import api.ipa.dto.PasteRequest;
import api.ipa.service.PasteNameGeneratorService;
import api.ipa.service.PasteService;
import api.ipa.service.StorageService;
import api.ipa.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class PasteFacade {

    private final PasteService pasteService;

    private final UserService userService;

    private final StorageService storageService;

    private final PasteNameGeneratorService nameGeneratorService;

    //TODO add RateLimiterService, FeedService, ApplicationEventPublisher delete as added

    public String createPaste(PasteRequest request){

    }
}
