package api.ipa.service.moderation;

import api.ipa.entity.Paste;
import api.ipa.entity.helpEntity.PasteStatus;
import api.ipa.repository.PasteRepository;
import api.ipa.service.StorageService;
import api.ipa.service.moderation.helpEntity.PasteCheckEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModerationContentWorker {
    private final StorageService storageService;
    private final PasteRepository pasteRepository;
    private final ModerationStrategyResolver strategyResolver;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleCreatedPaste(PasteCheckEvent event){
        try {
            log.info("Checking paste WITH KEY {} !!!!", event.storageKey());
            Paste paste = pasteRepository.findByStorageKey(event.storageKey()).orElseThrow();
            int trustScore = 100;

            ModerationStrategy strategy = strategyResolver.resolve(trustScore);
            log.info("Checking paste {} using {}", event.storageKey(), strategy.getClass().getSimpleName());

            String content = "";
            try(InputStream is = storageService.download(event.storageKey())){
                content = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
            }catch(IOException e){
                throw new RuntimeException();
            }
            boolean isHarmful = strategy.isHarmful(content);

            if (isHarmful) {
                pasteRepository.updatePasteStatus(paste.getId(), PasteStatus.REJECTED);
                log.warn("Paste {} REJECTED by moderation.", event.storageKey());
            } else {
                pasteRepository.updatePasteStatus(paste.getId(), PasteStatus.ACTIVE);
            }

        } catch (Exception e) {
            log.error("Moderation pipeline failed for paste {}.", event.storageKey(), e);
        }
    }
}
