package api.ipa.service;

import api.ipa.dto.UserPage;
import api.ipa.dto.UserUpdateRequest;
import api.ipa.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    Optional<User> findUser(Long id);
    UserUpdateRequest updateUserInfo(Long userId, UserUpdateRequest userRequest);
    Boolean deleteUser(Long id);
    Boolean deleteUser(User user);
    Boolean saveUser(User user);
    UserPage getUserPage(String username, User user);
    void uploadAvatar(Long userId, MultipartFile avatar);
    String resetAvatarToDefault(Long userId);
    void deleteAvatarByAdmin(Long userId);
}
