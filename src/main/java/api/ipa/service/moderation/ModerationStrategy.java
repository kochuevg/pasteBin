package api.ipa.service.moderation;

public interface ModerationStrategy {
    boolean appliesTo(int score);
    boolean isHarmful(String content);
}
