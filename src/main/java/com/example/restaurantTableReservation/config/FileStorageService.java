package com.example.restaurantTableReservation.config;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final UploadStoragePaths uploadStoragePaths;

    public FileStorageService(UploadStoragePaths uploadStoragePaths) {
        this.uploadStoragePaths = uploadStoragePaths;
    }

    public String storeRestaurantImage(Long restaurantId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPEG, PNG, and WebP images are allowed.");
        }

        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };

        String filename = UUID.randomUUID() + extension;
        Path directory = uploadStoragePaths.restaurantImagesRoot().resolve(String.valueOf(restaurantId));
        Files.createDirectories(directory);

        Path target = directory.resolve(filename);
        file.transferTo(target);

        return uploadStoragePaths.toPublicPath(target);
    }

    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path filePath = uploadStoragePaths.uploadsRoot().resolve(relativePath).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // best-effort cleanup
        }
    }
}
