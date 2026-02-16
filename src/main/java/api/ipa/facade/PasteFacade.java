package api.ipa.facade;

import api.ipa.dto.PasteRequest;
import api.ipa.dto.PasteResponse;
import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.service.PasteNameGeneratorService;
import api.ipa.service.PasteService;
import api.ipa.service.StorageService;
import api.ipa.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
        User creator = userService.findUser(request.userId()).orElseThrow(RuntimeException::new);
        //Check for disturbing content
        //
        String uniqueName = generateUniqueName().orElseThrow(RuntimeException::new);

        storageService.upload(uniqueName, request.data());

        Paste createdPaste = pasteService.createPaste(request, uniqueName, creator);

        creator.getPastes().add(createdPaste);
        userService.saveUser(creator);
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

    public PasteResponse getPaste(String storageKey){
        Paste paste = pasteService.findPasteByStorageKey(storageKey).orElseThrow(RuntimeException::new);

        String data;
        try(InputStream is = storageService.download(storageKey)){
            data = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }catch(IOException e){
            throw new RuntimeException();
        }

        return  PasteResponse.from(paste, data, paste.getLogs().size());
    }
}
