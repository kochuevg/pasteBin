package api.ipa.controller;

import api.ipa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @DeleteMapping("/users/{userId}/avatar")
    public ResponseEntity<Void> deleteOffensiveAvatar(@PathVariable Long userId) {

        userService.deleteAvatarByAdmin(userId);

        return ResponseEntity.noContent().build();
    }
}
