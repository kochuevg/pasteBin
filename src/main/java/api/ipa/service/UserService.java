package api.ipa.service;

import api.ipa.dto.UserRequest;
import api.ipa.entity.User;

public interface UserService {
    User findUser(Long id);
    User updateUser(Long id, UserRequest userRequest);
    Boolean deleteUser(Long id);
}
