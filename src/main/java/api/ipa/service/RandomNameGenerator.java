package api.ipa.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class RandomNameGenerator implements PasteNameGeneratorService{

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ID_LENGTH = 8;

    @Override
    public String generateName() {
        StringBuilder builder = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(randomIndex));
        }
        return builder.toString();
    }
}
