package api.ipa.service.moderation;

import api.ipa.exception.ForbiddenOperationException;
import org.springframework.stereotype.Component;

@Component
public class WordCheckModerationStrategy implements ModerationStrategy{
    private static final String[] BANNED_WORDS = {"badword1", "badword2"};

    @Override
    public boolean appliesTo(int score) {
        return score >= 50;
    }

    @Override
    public boolean isHarmful(String content) {
        if (content == null || content.isBlank()) {
            throw new ForbiddenOperationException("Forbidden moderation");
        }

        String[] words = content.split("\\s+");
        int totalWords = words.length;
        int badWordCount = 0;

        for (String bannedWord : BANNED_WORDS) {
            int occurrences = content.toLowerCase().split(bannedWord, -1).length - 1;
            badWordCount += occurrences;
        }

        double badWordPercentage = (double) badWordCount / totalWords;

        if (badWordPercentage > 0.05) {
            return true;
        }

        return false;
    }
}
