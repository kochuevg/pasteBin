package api.ipa.repository;

import api.ipa.entity.Paste;
import api.ipa.entity.helpEntity.PasteVisibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasteRepository extends JpaRepository<Paste, Long> {
    Optional<Paste> findByStorageKey(String key);
    List<Paste> findAllByExpirationDateBeforeAndDeleteAfterExpirationTrue(Instant now);
    List<Paste> findByVisibilityOrderByCreationDate(PasteVisibility visibility, Pageable pageable);
    @Query(value = """
        UPDATE Paste p SET p.views = p.views + :newViews\s
        WHERE p.storageKey = :storageKey
        """
    )
    void addViewsToPaste(String storageKey, Long newViews);
}
