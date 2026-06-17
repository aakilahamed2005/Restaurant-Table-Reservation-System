package com.example.restaurantTableReservation.Restaurant_Mangement.repository;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantScheduleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantScheduleRepository extends JpaRepository<RestaurantScheduleModel, Long> {

    /**
     * Retrieve all schedule entries for the given restaurant.
     * Spring Data returns an empty list when no entries exist (never null).
     */
    // ✅ Spring Data JPA guarantees this returns empty list, never null
    List<RestaurantScheduleModel> findByRestaurant(RestaurantModel restaurant);

    /**
     * Retrieve the schedule entry for a specific day of week for the
     * provided restaurant. Returns empty Optional if no schedule exists
     * for that day.
     */
    Optional<RestaurantScheduleModel> findByRestaurantAndDay(RestaurantModel restaurant, DayOfWeek day);
}