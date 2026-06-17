package com.example.restaurantTableReservation.Seat_Management.repository;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Seat_Management.model.SeatModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatRepository extends JpaRepository<SeatModel, Long> {

    Optional<SeatModel> findByRestaurant(RestaurantModel restaurant);
}
