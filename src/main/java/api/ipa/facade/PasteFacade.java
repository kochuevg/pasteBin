package api.ipa.facade;

import api.ipa.dto.PasteRequest;
import api.ipa.dto.PasteResponse;
import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.PasteVisibility;
import api.ipa.exception.ForbiddenOperationException;
import api.ipa.exception.PasteExpiredException;
import api.ipa.exception.PasteNotFoundException;
import api.ipa.exception.UserNotFoundException;
import api.ipa.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class PasteFacade {

    private final PasteService pasteService;

    private final StorageService storageService;

    private final PasteNameGeneratorService nameGeneratorService;

    private final RedisService redisService;

    //TODO add RateLimiterService, ApplicationEventPublisher delete as added

    public String createPaste(PasteRequest request, User creator){
        if(creator == null){
            throw new ForbiddenOperationException("You must be logged in to create pastes");
        }
        //Check for disturbing content
        //
        String uniqueName = generateUniqueName().orElseThrow(RuntimeException::new);

        log.info("Generated unique name:{} for request: {}", uniqueName, request);

        storageService.upload(uniqueName, request.data());

        log.info("Paste was uploaded to storage for request: {}", request);

        Paste createdPaste = pasteService.createPaste(request, uniqueName, creator);
        pasteService.save(createdPaste);

        log.info("Paste was successfully saved: {}", createdPaste);
        //Push to the cache and feed if its visible
        //update rate limit
        return uniqueName;
    }

    private Optional<String> generateUniqueName(){
        for(int i = 0; i < PasteNameGeneratorService.ATTEMPTS; i++){
            String nameToCheck = nameGeneratorService.generateName();
            Optional<Paste> foundPaste = pasteService.findPasteByStorageKey(nameToCheck);
            if(foundPaste.isEmpty()) return Optional.of(nameToCheck);
        }
        return Optional.empty();
    }

    public PasteResponse getPaste(String storageKey, User currentUser, String userIp, String userAgent){
        Paste paste = pasteService.findPasteByStorageKey(storageKey).orElseThrow(
                () -> new PasteNotFoundException(storageKey)
        );

        boolean isOwner = currentUser != null && currentUser.getId().equals(paste.getCreator().getId());

        if(paste.getExpirationDate().isBefore(Instant.now()) && !isOwner){
            throw new PasteExpiredException(storageKey);
        }

        if(paste.getVisibility() == PasteVisibility.PRIVATE && !isOwner){
            throw new ForbiddenOperationException("This paste is not publicly accessible");
        }

        redisService.recordUniqueView(storageKey, userIp, userAgent);

        String data = "";
        try(InputStream is = storageService.download(storageKey)){
            data = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }catch(IOException e){
            throw new RuntimeException();
        }

        return  PasteResponse.from(paste, data);
    }

    public boolean deletePaste(String key, User user){
        Paste paste = pasteService.findPasteByStorageKey(key).orElse(null);
        if(paste == null){
            return false;
        }

        if(!paste.getCreator().getId().equals(user.getId())){
            throw new ForbiddenOperationException("Forbidden delete");
        }

        storageService.delete(key);

        pasteService.deletePaste(paste);

        return true;
    }
}
