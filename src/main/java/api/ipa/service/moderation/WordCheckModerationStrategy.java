package api.ipa.service.moderation;

import api.ipa.exception.ForbiddenOperationException;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WordCheckModerationStrategy implements ModerationStrategy{
    @Value("classpath:banned-words.txt")
    private Resource bannedWordsFile;

    private final List<String> bannedWords = new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("Loading banned words from file...");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(bannedWordsFile.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String cleanWord = line.trim().toLowerCase();
                if (!cleanWord.isEmpty()) {
                    bannedWords.add(cleanWord);
                }
            }
            log.info("Successfully loaded {} banned words into memory.", bannedWords.size());

        } catch (Exception e) {
            log.error("Failed to load banned words file! Moderation might be compromised.", e);
        }
    }

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

        for (String bannedWord : bannedWords) {
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
