package api.ipa.service;

public interface PasteNameGeneratorService {
    int ATTEMPTS = 3;
    String generateName();
}
