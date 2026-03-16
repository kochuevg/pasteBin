package api.ipa.entity.helpEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record ExpirationFormat(
        Integer amount,
        ExpirationUnit timeUnit
) {
    public Instant calculateExpirationTime() {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Expiration amount must be greater than zero.");
        }

        return switch (timeUnit) {
            case MINUTES -> {
                if (amount > 1440) throw new IllegalArgumentException("Custom minutes must be between 1 and 59.");
                yield Instant.now().plus(amount, ChronoUnit.MINUTES);
            }
            case HOURS -> {
                if (amount > 23) throw new IllegalArgumentException("Custom hours must be between 1 and 23.");
                yield Instant.now().plus(amount, ChronoUnit.HOURS);
            }
            case DAYS -> {
                if (!List.of(1, 3, 7).contains(amount)) throw new IllegalArgumentException("Days can only be exactly 1, 3, or 7.");
                yield Instant.now().plus(amount, ChronoUnit.DAYS);
            }
            case MONTHS -> {
                if (amount != 1) throw new IllegalArgumentException("Months can only be exactly 1.");
                yield Instant.now().plus(30L * amount, ChronoUnit.DAYS);
            }
            case NEVER -> null;
        };
    }
}
