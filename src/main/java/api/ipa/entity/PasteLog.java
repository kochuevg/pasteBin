package api.ipa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class PasteLog {
    //TODO make a complex id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Instant viewedAt;
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paste_id")
    @JsonBackReference
    private Paste paste;
}
