package api.ipa.service;

import api.ipa.dto.AuthenticationRequest;
import api.ipa.dto.AuthenticationResponse;
import api.ipa.dto.RegisterRequest;
import api.ipa.entity.User;
import api.ipa.entity.helpEntity.Role;
import api.ipa.exception.AlreadyTakenException;
import api.ipa.exception.UserNotFoundException;
import api.ipa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        User user = userRepository.findByEmail(authenticationRequest.email()).orElseThrow(
                () -> new UserNotFoundException("User with email: "+ authenticationRequest.email() + "not found")
        );

        if(passwordEncoder.matches(authenticationRequest.password(), user.getPassword())) {
            String token = jwtService.generateToken(user);
            return new AuthenticationResponse(token);
        }

        throw new UserNotFoundException("Wrong password");
    }

    @Override
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        if(userRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new AlreadyTakenException("Email: " + registerRequest.email() + "is already taken");
        }

        if(userRepository.findByUsername(registerRequest.username()).isPresent()) {
            throw new AlreadyTakenException("Username: " + registerRequest.username() + "is already taken");
        }

        User user = User.builder()
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .role(Role.USER)
                .birthday(registerRequest.birthday())
                .build();

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        return new AuthenticationResponse(jwtToken);
    }
}
