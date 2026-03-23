package api.ipa.service;

import api.ipa.dto.UserPage;
import api.ipa.dto.UserUpdateRequest;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.Role;
import api.ipa.exception.UserNotFoundException;
import api.ipa.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    private final S3StorageService storageService;

    @Value("${app.user.avatar.max-size-mb}")
    private int maxAvatarSizeMb;

    @Value("${app.user.avatar.storage.public-url}")
    private String storagePublicUrl;

    @Override
    public Optional<User> findUser(Long id){
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public UserUpdateRequest updateUserInfo(Long trustedUserId, UserUpdateRequest request) {
        User user = userRepository.findById(trustedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.bio() != null) {
            user.setBio(request.bio());
        }

        userRepository.save(user);

        return new UserUpdateRequest(user.getBio());
    }

    @Override
    public Boolean deleteUser(Long id) {
        userRepository.deleteById(id);
        return true;
    }

    @Override
    public Boolean deleteUser(User user){
        if(user == null){
            return false;
        }
        userRepository.delete(user);
        return true;
    }

    @Override
    public Boolean saveUser(User user) {
        userRepository.save(user);
        return true;
    }

    @Override
    public UserPage getUserPage(String username, User user) {
        User foundUser = userRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("User with username: " + username + "not found")
        );

        boolean fetchAll = false;
        boolean isAdmin = false;
        String fullAvatarUrl = null;

        if (user != null) {
            if (user.getAvatarKey() != null) {
                fullAvatarUrl = storagePublicUrl + "/" + user.getAvatarKey();
            } else {
                fullAvatarUrl = "https://ui-avatars.com/api/?name=" + user.getUsername();
            }
            fetchAll = user.getId().equals(foundUser.getId());
            isAdmin = user.getRole() == Role.ADMIN;
        }

        if(isAdmin || fetchAll){
            return UserPage.toUserPageWithAllData(foundUser, fullAvatarUrl);
        }
        return UserPage.toUserPageWithPublicData(foundUser, fullAvatarUrl);

    }

    @Override
    public void uploadAvatar(Long userId, MultipartFile avatar) {
        long maxBytes = maxAvatarSizeMb * 1024L * 1024L;
        if (avatar.getSize() > maxBytes) {
            throw new IllegalArgumentException("Avatars cannot exceed " + maxAvatarSizeMb + "MB.");
        }

        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only images are allowed!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldAvatarKey = user.getAvatarKey();

        String originalFilename = avatar.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".png";
        String objectKey = "avatars/user-" + userId + "-" + UUID.randomUUID() + extension;

        storageService.upload(objectKey, avatar);

        user.setAvatarKey(objectKey);
        userRepository.save(user);

        if (oldAvatarKey != null) {
            log.info("Deleting old avatar for user {}: {}", userId, oldAvatarKey);
            storageService.delete(oldAvatarKey);
        }

        log.info("User {} successfully updated their avatar.", userId);

    }

    @Override
    public String resetAvatarToDefault(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldAvatarKey = user.getAvatarKey();

        if (oldAvatarKey != null) {
            log.info("User {} is resetting their avatar to default.", userId);
            storageService.delete(oldAvatarKey);

            user.setAvatarKey(null);
            userRepository.save(user);
        }

        return "https://ui-avatars.com/api/?name=" + user.getUsername();
    }

    @Override
    @Transactional
    public void deleteAvatarByAdmin(Long targetUserId) {
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String badAvatarKey = user.getAvatarKey();

        if (badAvatarKey != null) {
            log.info("Admin deleting offensive avatar for user {}", targetUserId);

            storageService.delete(badAvatarKey);

            user.setAvatarKey(null);
            userRepository.save(user);
        } else {
            log.warn("Admin tried to delete avatar for user {}, but none existed.", targetUserId);
        }
    }
}
