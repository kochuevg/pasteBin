package api.ipa.entity;

import api.ipa.entity.helpEntity.PasteFormat;
import api.ipa.entity.helpEntity.PasteVisibility;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paste {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String storageKey;

    private String title;

    private Boolean deleteAfterExpiration;

    @Enumerated(EnumType.STRING)
    private PasteVisibility visibility;

    private Instant creationDate;

    private Instant expirationDate;

    @Enumerated(EnumType.STRING)
    private PasteFormat pasteFormat;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "creator_id")
    private User creator;

    @OneToMany(mappedBy = "paste", fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JsonManagedReference
    private List<PasteLog> logs = new ArrayList<>();
}
