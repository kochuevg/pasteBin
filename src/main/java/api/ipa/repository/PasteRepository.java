package api.ipa.repository;

import api.ipa.entity.Paste;
import api.ipa.entity.helpEntity.PasteStatus;
import api.ipa.entity.helpEntity.PasteVisibility;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasteRepository extends JpaRepository<Paste, Long> {
    Optional<Paste> findByStorageKey(String key);
    List<Paste> findAllByExpirationDateBeforeAndDeleteAfterExpirationTrue(Instant now);
    List<Paste> findByVisibilityOrderByCreationDate(PasteVisibility visibility, Pageable pageable);

    @Modifying
    @Query(value = """
        UPDATE Paste p SET p.views = p.views + :newViews\s
        WHERE p.storageKey = :storageKey
        """
    )
    void addViewsToPaste(String storageKey, Long newViews);

    @Modifying
    @Query(value = """
       UPDATE Paste p SET p.status = :newStatus
       WHERE p.id = :pasteId\s
""")
    void updatePasteStatus(Long pasteId, PasteStatus newStatus);

    @Query(value = """
        select p from Paste p\s
        where (p.expirationDate <= :now and p.deleteAfterExpiration) or p.status = 'REJECTED'
""")
    List<Paste> findAllForDeletion(Instant now);

    @Query("SELECT COUNT(p) FROM Paste p WHERE p.creator.id = :userId AND p.expirationDate IS NULL")
    long countNonExpiringPastesByUser(@Param("userId") Long userId);
}
