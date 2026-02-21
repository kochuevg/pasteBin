package api.ipa.repository;

import api.ipa.entity.Paste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasteRepository extends JpaRepository<Paste, Long> {
    Optional<Paste> findByStorageKey(String key);
    List<Paste> findAllByExpirationDateBeforeAndDeleteAfterExpirationTrue(Instant now);
}
