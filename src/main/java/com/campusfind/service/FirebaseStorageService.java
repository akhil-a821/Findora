package com.campusfind.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageService.class);

    @Value("${firebase.bucket.name:}")
    private String bucketName;

    public boolean isFirebaseEnabled() {
        return !FirebaseApp.getApps().isEmpty() && bucketName != null && !bucketName.isBlank();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (!isFirebaseEnabled()) {
            throw new IllegalStateException("Firebase Storage is not enabled or bucket name is missing.");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!fileExtension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("Only image files (JPG, PNG, GIF, WEBP) are allowed.");
        }

        String blobName = "items/" + UUID.randomUUID().toString() + fileExtension;
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";

        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket.getName(), blobName))
                .setContentType(contentType)
                .build();

        bucket.getStorage().create(blobInfo, file.getBytes(), Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));

        String encodedBlobName = URLEncoder.encode(blobName, StandardCharsets.UTF_8);
        String publicUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucket.getName(), encodedBlobName);

        logger.info("Successfully uploaded image to Firebase Storage: {}", publicUrl);
        return publicUrl;
    }
}
