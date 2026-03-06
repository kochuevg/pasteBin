package api.ipa.service.moderation;

import org.springframework.stereotype.Component;

@Component
public class AIModerationStrategy implements ModerationStrategy{
    @Override
    public boolean appliesTo(int score) {
        return false;
    }

    @Override
    public boolean isHarmful(String content) {
        return false;
    }
}
