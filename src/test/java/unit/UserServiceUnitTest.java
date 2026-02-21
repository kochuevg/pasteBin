package unit;

import api.ipa.dto.UserPage;
import api.ipa.entity.Paste;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.PasteVisibility;
import api.ipa.entity.helpEntity.Role;
import api.ipa.exception.UserNotFoundException;
import api.ipa.repository.UserRepository;
import api.ipa.service.UserServiceImpl;
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
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User owner;
    private Paste publicPaste;
    private Paste privatePaste;

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

        privatePaste = new Paste();
        privatePaste.setId(200L);
        privatePaste.setStorageKey("secret-key1");
        privatePaste.setCreator(owner);
        privatePaste.setVisibility(PasteVisibility.PRIVATE);

        owner.getPastes().add(publicPaste);
        owner.getPastes().add(privatePaste);
    }

    @Test
    void shouldFindUserWhenUserExists() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        Optional<User> found = userService.findUser(1L);

        assertTrue(found.isPresent());
        assertEquals("owner", found.get().getUsername());
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<User> found = userService.findUser(99L);

        assertFalse(found.isPresent());
    }

    @Test
    void shouldDeleteUserByIdAndReturnTrue() {
        Boolean result = userService.deleteUser(1L);

        assertTrue(result);
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(1L);
    }

    @Test
    void shouldReturnFalseWhenDeletingNullUserObject() {
        Boolean result = userService.deleteUser((User) null);

        assertFalse(result);
        Mockito.verify(userRepository, Mockito.never()).delete(Mockito.any());
    }

    @Test
    void shouldDeleteUserObjectAndReturnTrue() {
        Boolean result = userService.deleteUser(owner);

        assertTrue(result);
        Mockito.verify(userRepository, Mockito.times(1)).delete(owner);
    }

    @Test
    void shouldSaveUserAndReturnTrue() {
        Boolean result = userService.saveUser(owner);

        assertTrue(result);
        Mockito.verify(userRepository, Mockito.times(1)).save(owner);
    }

    @Test
    void shouldThrowExceptionWhenGettingPageForUnknownUser() {
        Mockito.when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserPage("unknown", owner)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void shouldReturnAllDataWhenRequesterIsAdmin() {
        User adminRequester = new User();
        adminRequester.setId(99L);
        adminRequester.setRole(Role.ADMIN);

        Mockito.when(userRepository.findByUsername("owner"))
                .thenReturn(Optional.of(owner));

        UserPage result = userService.getUserPage("owner", adminRequester);

        assertNotNull(result);
        assertEquals(2, result.pastes().size());
    }

    @Test
    void shouldReturnAllDataWhenRequesterIsTheOwner() {
        Mockito.when(userRepository.findByUsername("owner"))
                .thenReturn(Optional.of(owner));

        UserPage result = userService.getUserPage("owner", owner);

        assertNotNull(result);
        assertEquals(2, result.pastes().size());
    }
}
