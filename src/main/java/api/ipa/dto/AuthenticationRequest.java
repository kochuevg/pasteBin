package api.ipa.dto;

public record AuthenticationRequest(
        String email,
        String password
) {
}
