package api.ipa.service;

import api.ipa.dto.UserRequest;
import api.ipa.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findUser(Long id);
    User updateUserInfo(Long id, UserRequest userRequest);
    Boolean deleteUser(Long id);
    Boolean saveUser(User user);
}
