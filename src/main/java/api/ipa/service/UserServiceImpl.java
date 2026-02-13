package api.ipa.service;

import api.ipa.dto.UserRequest;
import api.ipa.entity.User;
import api.ipa.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    public User findUser(Long id){
        User user = userRepository.findById(id).orElseThrow(NullPointerException::new);
        return user;
    }

    @Override
    public User updateUser(Long id, UserRequest userRequest) {
        return null;
    }

    @Override
    public Boolean deleteUser(Long id) {
        return null;
    }
}
