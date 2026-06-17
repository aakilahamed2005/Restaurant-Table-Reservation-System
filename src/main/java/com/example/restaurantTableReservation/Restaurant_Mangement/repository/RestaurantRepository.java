package com.example.restaurantTableReservation.Restaurant_Mangement.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.Status;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for `RestaurantModel` providing common data access
 * methods. Extends `JpaRepository` for CRUD and paging, and
 * `JpaSpecificationExecutor` for dynamic queries.
 */
public interface RestaurantRepository extends JpaRepository<RestaurantModel, Long>,
        JpaSpecificationExecutor<RestaurantModel> {

    /**
     * Find all restaurants owned by the specified user.
     * Typical use: list restaurants for an owner dashboard.
     */
    List<RestaurantModel> findByOwner(UserModel owner);

    /**
     * Find restaurants by `Status` with support for paging and sorting.
     * Example: fetch the top-rated `ACTIVE` restaurants using a pageable
     * that sorts by `overallRating` descending.
     */
    Page<RestaurantModel> findByStatus(Status status, Pageable pageable);
}
