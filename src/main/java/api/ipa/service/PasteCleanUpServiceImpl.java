package api.ipa.service;

import api.ipa.entity.Paste;
import api.ipa.repository.PasteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PasteCleanUpServiceImpl implements PasteCleanUpService {
    private final StorageService storageService;
    private final PasteRepository pasteRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Override
    public void cleanExpiredPastes() {
        log.info("Starting scheduled cleanup of expired pastes...");

        List<Paste> expiredPastes = pasteRepository.findAllByExpirationDateBeforeAndDeleteAfterExpirationTrue(Instant.now());

        if (expiredPastes.isEmpty()) {
            log.info("No expired pastes found. Cleanup complete.");
            return;
        }

        int deletedCount = 0;

        for (Paste paste : expiredPastes) {
            try {
                storageService.delete(paste.getStorageKey());
                pasteRepository.delete(paste);
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete expired paste with key: {}", paste.getStorageKey(), e);
            }
        }

        log.info("Cleanup complete. Successfully deleted {} expired pastes.", deletedCount);
    }
}
