package api.ipa.repository;

import api.ipa.entity.Paste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasteRepository extends JpaRepository<Paste, Long> {
}
