package com.example.restaurantTableReservation.Feedback_Management;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackModel, Long> {
    int countByRestaurant(RestaurantModel restaurant);
    List<FeedbackModel> findByRestaurant(RestaurantModel restaurant);
    List<FeedbackModel> findByRestaurant_IdOrderByCreatedAtDesc(Long restaurantId);
    List<FeedbackModel> findByCustomer(UserModel customer);
    List<FeedbackModel> findByCustomer_Id(Long customerId);
}
