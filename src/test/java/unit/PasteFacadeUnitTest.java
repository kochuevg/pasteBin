package unit;

import api.ipa.dto.PasteRequest;
import api.ipa.dto.PasteResponse;
import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.PasteFormat;
import api.ipa.entity.helpEntity.PasteVisibility;
import api.ipa.exception.ForbiddenOperationException;
import api.ipa.facade.PasteFacade;
import api.ipa.service.PasteNameGeneratorService;
import api.ipa.service.PasteService;
import api.ipa.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class PasteFacadeUnitTest {

    @Mock
    private PasteService pasteService;

    @Mock
    private StorageService storageService;

    @Mock
    private PasteNameGeneratorService nameGeneratorService;

    @InjectMocks
    private PasteFacade pasteFacade;

    private User owner;
    private User hacker;
    private Paste dummyPaste;

    private PasteRequest dummyRequest;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        hacker = new User();
        hacker.setId(2L);
        hacker.setUsername("hacker");

        dummyPaste = new Paste();
        dummyPaste.setId(100L);
        dummyPaste.setStorageKey("secret-key");
        dummyPaste.setCreator(owner);
        dummyPaste.setVisibility(PasteVisibility.PUBLIC);
        dummyPaste.setPasteFormat(PasteFormat.PLAIN_TEXT);
        dummyPaste.setLogs(new ArrayList<>());
        dummyPaste.setExpirationDate(Instant.MAX);

        dummyRequest = new PasteRequest("Hello World Data",
                PasteFormat.PLAIN_TEXT,
                PasteVisibility.PUBLIC,
                true,
                "SOME CONTENT",
                1L);

        owner.getPastes().add(dummyPaste);
    }

    @Test
    void createPasteThrowsExceptionWhenCreatorIsNull() {
        assertThrows(ForbiddenOperationException.class, () -> {
            pasteFacade.createPaste(dummyRequest, null);
        });
    }

    @Test
    void createPasteThrowsExceptionWhenNameGenerationFails() {
        Mockito.when(nameGeneratorService.generateName()).thenReturn("taken-name");
        Mockito.when(pasteService.findPasteByStorageKey("taken-name"))
                .thenReturn(Optional.of(new Paste()));

        assertThrows(RuntimeException.class, () -> {
            pasteFacade.createPaste(dummyRequest, owner);
        });
    }

    @Test
    void createPasteSuccessfullySavesAndReturnsUniqueName() {
        Mockito.when(nameGeneratorService.generateName()).thenReturn("new-unique-key");
        Mockito.when(pasteService.findPasteByStorageKey("new-unique-key"))
                .thenReturn(Optional.empty());

        Mockito.when(pasteService.createPaste(eq(dummyRequest), eq("new-unique-key"), eq(owner)))
                .thenReturn(dummyPaste);

        String resultName = pasteFacade.createPaste(dummyRequest, owner);

        assertEquals("new-unique-key", resultName);
        Mockito.verify(storageService).upload("new-unique-key", dummyRequest.data());
        Mockito.verify(pasteService).save(dummyPaste);
    }

    @Test
    void getPasteThrowsExceptionWhenPasteNotFound() {
        Mockito.when(pasteService.findPasteByStorageKey("missing-key"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            pasteFacade.getPaste("missing-key", owner);
        });
    }

    @Test
    void getPasteThrowsExceptionWhenPasteIsPrivateAndUserIsNotOwner() {
        dummyPaste.setVisibility(PasteVisibility.PRIVATE);
        Mockito.when(pasteService.findPasteByStorageKey("private-key"))
                .thenReturn(Optional.of(dummyPaste));

        assertThrows(ForbiddenOperationException.class, () -> {
            pasteFacade.getPaste("private-key", hacker);
        });
    }

    @Test
    void getPasteThrowsExceptionWhenStorageFails() throws IOException {
        Mockito.when(pasteService.findPasteByStorageKey("valid-key"))
                .thenReturn(Optional.of(dummyPaste));

        InputStream poisonedStream = Mockito.mock(InputStream.class);

        Mockito.when(poisonedStream.read(any(byte[].class)))
                .thenThrow(new IOException("MinIO connection lost during read!"));

        Mockito.when(storageService.download("valid-key"))
                .thenReturn(poisonedStream);

        assertThrows(RuntimeException.class, () -> {
            pasteFacade.getPaste("valid-key", owner);
        });
    }

    @Test
    void getPasteSuccessfullyReturnsPasteResponse() throws IOException {
        Mockito.when(pasteService.findPasteByStorageKey("valid-key"))
                .thenReturn(Optional.of(dummyPaste));

        InputStream fakeStream = new ByteArrayInputStream("File Content".getBytes(StandardCharsets.UTF_8));
        Mockito.when(storageService.download("valid-key")).thenReturn(fakeStream);

        PasteResponse response = pasteFacade.getPaste("valid-key", owner);

        assertNotNull(response);
        assertEquals("File Content", response.data());
    }

    @Test
    void deletePasteReturnsFalseWhenPasteNotFound() {
        Mockito.when(pasteService.findPasteByStorageKey("missing-key"))
                .thenReturn(Optional.empty());

        boolean result = pasteFacade.deletePaste("missing-key", owner);

        assertFalse(result);
        Mockito.verify(storageService, Mockito.never()).delete(anyString());
    }

    @Test
    void deletePasteThrowsExceptionWhenUserIsNotOwner() {
        Mockito.when(pasteService.findPasteByStorageKey("valid-key"))
                .thenReturn(Optional.of(dummyPaste));

        assertThrows(ForbiddenOperationException.class, () -> {
            pasteFacade.deletePaste("valid-key", hacker);
        });

        Mockito.verify(storageService, Mockito.never()).delete(anyString());
        Mockito.verify(pasteService, Mockito.never()).deletePaste(any());
    }

    @Test
    void deletePasteSuccessfullyDeletesAndReturnsTrue() {
        Mockito.when(pasteService.findPasteByStorageKey("valid-key"))
                .thenReturn(Optional.of(dummyPaste));

        boolean result = pasteFacade.deletePaste("valid-key", owner);

        assertTrue(result);
        Mockito.verify(storageService).delete("valid-key");
        Mockito.verify(pasteService).deletePaste(dummyPaste);
    }
}