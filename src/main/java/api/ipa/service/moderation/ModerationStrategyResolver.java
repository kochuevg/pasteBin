package api.ipa.service.moderation;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModerationStrategyResolver {
    private final List<ModerationStrategy> strategies;

    public ModerationStrategyResolver(List<ModerationStrategy> strategies) {
        this.strategies = strategies;
    }

    public ModerationStrategy resolve(int score) {
        return strategies.stream()
                .filter(strategy -> strategy.appliesTo(score))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No moderation strategy found for trust score: " + score));
    }
}
