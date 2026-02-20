package api.ipa.dto;

import java.time.LocalDateTime;

public record RegisterRequest(
        String email,
        String password,
        String username,
        LocalDateTime birthday
) {
}
