package com.example.restaurantTableReservation.Restaurant_Mangement.repository;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantImageModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for `RestaurantImageModel` providing common queries used by
 * the application. Methods rely on Spring Data JPA method name parsing
 * to generate SQL for filtering and ordering images.
 */
public interface RestaurantImageRepository extends JpaRepository<RestaurantImageModel, Long> {

    /**
     * Retrieve all images for the given restaurant ordered by `sortOrder` ascending.
     * Useful for displaying images in the intended sequence.
     */
    List<RestaurantImageModel> findByRestaurantOrderBySortOrderAsc(RestaurantModel restaurant);

    /**
     * Retrieve images for multiple restaurants (by id list) ordered by `sortOrder`.
     * Useful when rendering collections of restaurants and their images.
     */
    List<RestaurantImageModel> findByRestaurant_IdInOrderBySortOrderAsc(List<Long> restaurantIds);

    /**
     * Find a specific image by id and ensure it belongs to the provided restaurant.
     * This is handy for secure lookups before performing updates or deletes.
     */
    Optional<RestaurantImageModel> findByIdAndRestaurant(Long id, RestaurantModel restaurant);

    /**
     * Delete all images associated with the given restaurant. Implemented by
     * Spring Data JPA via method name parsing.
     */
    void deleteByRestaurant(RestaurantModel restaurant);
}
