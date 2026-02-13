package api.ipa.repository;

import api.ipa.entity.PasteLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasteLogRepository extends JpaRepository<PasteLog, Long> {
}
