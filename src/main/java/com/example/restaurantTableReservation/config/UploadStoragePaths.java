package com.example.restaurantTableReservation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadStoragePaths {

    private final Path uploadsRoot;
    private final Path restaurantImagesRoot;

    public UploadStoragePaths(@Value("${app.upload.restaurant-images.dir}") String restaurantImagesDir) {
        this.restaurantImagesRoot = Paths.get(restaurantImagesDir).toAbsolutePath().normalize();
        this.uploadsRoot = restaurantImagesRoot.getParent();
    }

    public Path uploadsRoot() {
        return uploadsRoot;
    }

    public Path restaurantImagesRoot() {
        return restaurantImagesRoot;
    }

    public String toPublicPath(Path storedFile) {
        return uploadsRoot.relativize(storedFile).toString().replace('\\', '/');
    }
}
