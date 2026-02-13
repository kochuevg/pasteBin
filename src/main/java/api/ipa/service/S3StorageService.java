package api.ipa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService implements StorageService {
    //TODO change errors to custom

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public String upload(String key, String content) {
        log.info("Uploading paste to S3: {}", key);

        try {
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("text/plain")
                    .build();

            s3Client.putObject(putOb, RequestBody.fromString(content, StandardCharsets.UTF_8));

            return key;
        } catch (S3Exception e) {
            log.error("S3 Upload Error", e);
            throw new RuntimeException("Failed to upload to storage", e);
        }
    }

    @Override
    public InputStream download(String key) {
        log.info("Downloading paste from S3: {}", key);

        try {
            GetObjectRequest getOb = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            return s3Client.getObject(getOb);
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Content not found in storage");
        }
    }

    @Override
    public void delete(String key) {
        try {
            DeleteObjectRequest delOb = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(delOb);
        } catch (S3Exception e) {
            log.error("S3 Delete Error for key: {}", key, e);

        }
    }
}
