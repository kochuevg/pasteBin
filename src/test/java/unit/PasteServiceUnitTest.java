package unit;

import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.PasteVisibility;
import api.ipa.repository.PasteRepository;
import api.ipa.service.PasteService;
import api.ipa.service.PasteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasteServiceUnitTest {

    @Mock
    private PasteRepository pasteRepository;

    @InjectMocks
    private PasteServiceImpl pasteService;

    private User owner;
    private Paste publicPaste;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        publicPaste = new Paste();
        publicPaste.setId(100L);
        publicPaste.setStorageKey("secret-key");
        publicPaste.setCreator(owner);
        publicPaste.setVisibility(PasteVisibility.PUBLIC);
    }

    @Test
    void shouldFindPasteWhenItExists() {
        Mockito.when(pasteRepository.findByStorageKey("secret-key"))
                .thenReturn(Optional.of(publicPaste));

        Optional<Paste> found = pasteService.findPasteByStorageKey("secret-key");

        assertTrue(found.isPresent());
        assertEquals(owner.getId(), found.get().getCreator().getId());
    }

    @Test
    void shouldFindPasteWhenItDoesNotExist() {
        Mockito.when(pasteRepository.findByStorageKey("secret-key1"))
                .thenReturn(Optional.empty());

        Optional<Paste> found = pasteService.findPasteByStorageKey("secret-key1");

        assertFalse(found.isPresent());
        Mockito.verify(pasteRepository, Mockito.times(1)).findByStorageKey("secret-key1");
    }

    @Test
    void deletePaste() {
        pasteService.deletePaste(publicPaste);

        Mockito.verify(pasteRepository, Mockito.times(1)).delete(publicPaste);
    }

}