package api.ipa.service;

import api.ipa.dto.UserPage;
import api.ipa.dto.UserRequest;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.Role;
import api.ipa.exception.UserNotFoundException;
import api.ipa.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    public Optional<User> findUser(Long id){
        return userRepository.findById(id);
    }

    @Override
    public User updateUserInfo(Long id, UserRequest userRequest) {
        return null;
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

        if (user != null) {
            fetchAll = user.getId().equals(foundUser.getId());
            isAdmin = user.getRole() == Role.ADMIN;
        }

        if(isAdmin || fetchAll){
            return UserPage.toUserPageWithAllData(foundUser);
        }
        return UserPage.toUserPageWithPublicData(foundUser);

    }
}
