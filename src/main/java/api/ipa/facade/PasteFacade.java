package api.ipa.facade;

import api.ipa.dto.PasteRequest;
import api.ipa.dto.PasteResponse;
import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.PasteStatus;
import api.ipa.entity.helpEntity.PasteVisibility;
import api.ipa.exception.ForbiddenOperationException;
import api.ipa.exception.PasteExpiredException;
import api.ipa.exception.PasteNotFoundException;
import api.ipa.service.*;
import api.ipa.service.moderation.helpEntity.PasteCheckEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher applicationEventPublisher;

    //TODO add RateLimiterService, delete as added

    public String createPaste(PasteRequest request, User creator){
        if(creator == null){
            throw new ForbiddenOperationException("You must be logged in to create pastes");
        }
        String uniqueName = generateUniqueName().orElseThrow(RuntimeException::new);

        log.info("Generated unique name:{} for request: {}", uniqueName, request);

        storageService.upload(uniqueName, request.data());

        log.info("Paste was uploaded to storage for request: {}", request);

        Paste createdPaste = request.toPaste(uniqueName, creator);

        if(createdPaste.getVisibility() != PasteVisibility.PRIVATE){
            applicationEventPublisher.publishEvent(new PasteCheckEvent(uniqueName));
        }else{
            createdPaste.setStatus(PasteStatus.ACTIVE);
        }

        pasteService.save(createdPaste);

        log.info("Paste was successfully saved: {}", createdPaste.getStorageKey());
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
        PasteResponse cached = redisService.getPasteFromCache(storageKey);
        if(cached != null){
            log.info("Paste was found in cache REDIS: {}", cached.key());
            boolean cachedOwner = currentUser != null && currentUser.getUsername().equals(cached.ownerUsername());
            if(cached.expireAt().isBefore(Instant.now()) && cachedOwner){
                throw new PasteExpiredException(storageKey);
            }
        }

        Paste paste = pasteService.findPasteByStorageKey(storageKey).orElseThrow(
                () -> new PasteNotFoundException(storageKey)
        );

        boolean isOwner = currentUser != null && currentUser.getId().equals(paste.getCreator().getId());

        if(paste.getExpirationDate().isBefore(Instant.now()) && !isOwner){
            throw new PasteExpiredException(storageKey);
        }

        if((paste.getVisibility() == PasteVisibility.PRIVATE || paste.getStatus() == PasteStatus.PENDING) && !isOwner ){
            throw new ForbiddenOperationException("This paste is not publicly accessible");
        }

        log.info("Paste was found in database: {}", paste.getStorageKey());

        String data = "";
        try(InputStream is = storageService.download(storageKey)){
            data = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }catch(IOException e){
            throw new RuntimeException();
        }

        redisService.recordUniqueView(storageKey, userIp, userAgent);

        PasteResponse response = PasteResponse.from(paste, data, paste.getCreator().getUsername());

        if(paste.getVisibility() != PasteVisibility.PRIVATE && paste.getStatus() == PasteStatus.ACTIVE){
            log.info("Paste was uploaded in cache REDIS: {}", paste.getStorageKey());
            redisService.savePasteToCache(response);
        }

        return response;
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
