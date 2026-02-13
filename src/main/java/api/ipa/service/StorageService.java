package api.ipa.service;

import java.io.InputStream;

public interface StorageService {
    String upload(String key, String content);

    InputStream download(String key);

    void delete(String key);
}
