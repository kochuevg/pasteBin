package api.ipa.service;

import api.ipa.dto.AuthenticationRequest;
import api.ipa.dto.AuthenticationResponse;
import api.ipa.dto.RegisterRequest;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    AuthenticationResponse register(RegisterRequest registerRequest);
}
