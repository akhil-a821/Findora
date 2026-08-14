package com.campusfind.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:classpath:serviceAccountKey.json}")
    private String credentialsPath;

    @Value("${firebase.bucket.name:}")
    private String bucketName;

    private final ResourceLoader resourceLoader;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initializeFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            logger.info("Firebase Application is already initialized.");
            return;
        }

        try {
            Resource resource = resourceLoader.getResource(credentialsPath);
            if (!resource.exists()) {
                logger.info("Firebase service account credentials file not found at '{}'. Firebase Storage is disabled, defaulting to local disk file uploads.", credentialsPath);
                return;
            }

            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions.Builder builder = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount));

                if (bucketName != null && !bucketName.isBlank()) {
                    builder.setStorageBucket(bucketName);
                }

                FirebaseApp.initializeApp(builder.build());
                logger.info("Firebase App successfully initialized!");
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize Firebase App: {}. Falling back to local storage.", e.getMessage());
        }
    }
}
