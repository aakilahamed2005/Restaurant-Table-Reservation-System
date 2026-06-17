package com.example.restaurantTableReservation.Restaurant_Mangement;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantImageModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantImageRepository;
import com.example.restaurantTableReservation.config.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RestaurantImageService {

    // Maximum number of images allowed per restaurant
    public static final int MAX_IMAGES_PER_RESTAURANT = 10;

    @Autowired
    private RestaurantImageRepository restaurantImageRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<RestaurantImageModel> getImagesForRestaurant(RestaurantModel restaurant) {
        // Return all images for a restaurant ordered by the sortOrder (primary first)
        return restaurantImageRepository.findByRestaurantOrderBySortOrderAsc(restaurant);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getPrimaryImagePaths(List<RestaurantModel> restaurants) {
        if (restaurants == null || restaurants.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> ids = restaurants.stream()
                .map(RestaurantModel::getId)
                .filter(id -> id != null)
                .toList();

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        // Query images for all given restaurant ids (ordered by sort order)
        Map<Long, String> primaryPaths = new HashMap<>();
        List<RestaurantImageModel> images = restaurantImageRepository.findByRestaurant_IdInOrderBySortOrderAsc(ids);

        // For each restaurant keep the first encountered image path (primary image)
        for (RestaurantImageModel image : images) {
            Long restaurantId = image.getRestaurant().getId();
            primaryPaths.putIfAbsent(restaurantId, image.getFilePath());
        }

        return primaryPaths;
    }

    @Transactional
    public void saveImages(RestaurantModel restaurant, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        // Determine current count and available slots
        List<RestaurantImageModel> existing = restaurantImageRepository.findByRestaurantOrderBySortOrderAsc(restaurant);
        int nextSortOrder = existing.size();
        int availableSlots = MAX_IMAGES_PER_RESTAURANT - existing.size();

        if (availableSlots <= 0) {
            throw new IllegalArgumentException("Maximum of " + MAX_IMAGES_PER_RESTAURANT + " images allowed per restaurant.");
        }

        // Iterate uploaded files, store them, and create DB records until slots are exhausted
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue; // skip empty uploads
            }
            if (availableSlots <= 0) {
                break; // stop when we've reached the maximum
            }

            // Store file on disk (or cloud) and get stored path
            String filePath = fileStorageService.storeRestaurantImage(restaurant.getId(), file);
            if (filePath == null) {
                continue; // skip if storage failed for this file
            }

            // Persist image metadata with incrementing sort order
            RestaurantImageModel image = new RestaurantImageModel();
            image.setRestaurant(restaurant);
            image.setFilePath(filePath);
            image.setSortOrder(nextSortOrder++);
            restaurantImageRepository.save(image);
            availableSlots--;
        }
    }

    @Transactional
    public void deleteImages(RestaurantModel restaurant, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        // Delete requested images (remove file from storage and DB record)
        for (Long imageId : imageIds) {
            restaurantImageRepository.findByIdAndRestaurant(imageId, restaurant).ifPresent(image -> {
                fileStorageService.deleteFile(image.getFilePath());
                restaurantImageRepository.delete(image);
            });
        }

        // Reorder remaining images to keep contiguous sortOrder values
        reorderImages(restaurant);
    }

    @Transactional
    public void deleteAllImagesForRestaurant(RestaurantModel restaurant) {
        List<RestaurantImageModel> images = restaurantImageRepository.findByRestaurantOrderBySortOrderAsc(restaurant);
        // Remove files from storage and delete all DB records for the restaurant
        for (RestaurantImageModel image : images) {
            fileStorageService.deleteFile(image.getFilePath());
        }
        restaurantImageRepository.deleteByRestaurant(restaurant);
    }

    private void reorderImages(RestaurantModel restaurant) {
        List<RestaurantImageModel> images = restaurantImageRepository.findByRestaurantOrderBySortOrderAsc(restaurant);
        int order = 0;
        // Reassign sequential sortOrder values (0..n-1) and persist changes
        for (RestaurantImageModel image : images) {
            image.setSortOrder(order++);
            restaurantImageRepository.save(image);
        }
    }
}
