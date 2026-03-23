package api.ipa.controller;

import api.ipa.dto.UserPage;
import api.ipa.dto.UserUpdateRequest;
import api.ipa.entity.User;
import api.ipa.service.S3StorageService;
import api.ipa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;


    @PostMapping("/me/avatar")
    public ResponseEntity<Void> uploadAvatar(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file
            ) {

        userService.uploadAvatar(user.getId(), file);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> resetMyAvatar(
            @AuthenticationPrincipal User currentUser) {

        String fallbackUrl = userService.resetAvatarToDefault(currentUser.getId());

        return ResponseEntity.ok(Map.of("avatarUrl", fallbackUrl));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserUpdateRequest> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserUpdateRequest request) {

        UserUpdateRequest updatedProfile = userService.updateUserInfo(currentUser.getId(), request);

        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserPage> getUser(@PathVariable String username,
                        @AuthenticationPrincipal User user) {
        UserPage userPage = userService.getUserPage(username, user);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User user) {
        if(userService.deleteUser(user)){
            return ResponseEntity.ok("User successfully deleted");
        };
        return new ResponseEntity<>("User was not deleted", HttpStatus.BAD_REQUEST);
    }
}
