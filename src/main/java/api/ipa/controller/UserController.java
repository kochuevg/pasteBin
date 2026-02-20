package api.ipa.controller;

import api.ipa.dto.UserPage;
import api.ipa.entity.User;
import api.ipa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;

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
