package com.example.restaurantTableReservation.Restaurant_Mangement.model;

import jakarta.persistence.*;

/**
 * Represents an image associated with a restaurant. Images are ordered
 * using `sortOrder` and the file location is stored in `filePath`.
 */
@Entity
@Table(name = "restaurant_images")
public class RestaurantImageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many images belong to a single restaurant. Use LAZY loading
    // to avoid fetching image metadata unless needed.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantModel restaurant;

    // Path (relative or absolute depending on app config) to the image file
    @Column(name = "file_path", nullable = false)
    private String filePath;

    // Order used when displaying images (lower = shown earlier)
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public RestaurantImageModel() {
    }

    // Primary key getter/setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Restaurant relationship getter/setter
    public RestaurantModel getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantModel restaurant) {
        this.restaurant = restaurant;
    }

    // File path getter/setter
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Sort order getter/setter
    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
