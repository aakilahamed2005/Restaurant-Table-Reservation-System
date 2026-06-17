package com.example.restaurantTableReservation.Feedback_Management;


import com.example.restaurantTableReservation.Restaurant_Mangement.RestaurantService;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantRepository;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    RestaurantService restaurantService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;


    public FeedbackModel createFeedback(FeedbackDto feedback_form) {
        FeedbackModel feedback = new FeedbackModel();
        RestaurantModel restaurant = restaurantService.getRestaurantById(feedback_form.getRestaurantId());
        UserModel user = userRepository.getReferenceById(feedback_form.getCustomerId());

        feedback.setCustomer(user);
        feedback.setRestaurant(restaurant);

        feedback.setOverall_rating(feedback_form.getOverallRating());
        feedback.setFood_cuisine_rating(feedback_form.getFoodCuisineRating());
        feedback.setStaff_service_rating(feedback_form.getStaffServiceRating());
        feedback.setAmbience_atmosphere_rating(feedback_form.getAmbienceAtmosphereRating());
        feedback.setValue_for_money_rating(feedback_form.getValueForMoneyRating());
        feedback.setReview(feedback_form.getReview());

        FeedbackModel savedFeedback = feedbackRepository.save(feedback);
        recalculateRestaurantRating(restaurant);
        return savedFeedback;
    }

    // Retrieve all feedback entries for a specific restaurant
    public List<FeedbackModel> getAllFeedbackForRestaurant(RestaurantModel restaurant) {
        return feedbackRepository.findByRestaurant(restaurant);
    }


    // Retrieve feedbacks for a restaurant by restaurant ID,
    // ordered by newest feedback first
    public List<FeedbackModel> getFeedbacksForRestaurant(Long restaurantId) {
        return feedbackRepository.findByRestaurant_IdOrderByCreatedAtDesc(restaurantId);
    }


    // Retrieve all feedbacks submitted by a specific customer
    public List<FeedbackModel> getFeedbackByCustomerId(Long customerId) {
        return feedbackRepository.findByCustomer_Id(customerId);
    }


    // Retrieve a single feedback by its ID
    public FeedbackModel getFeedbackById(Long id) {
        Optional<FeedbackModel> opt = feedbackRepository.findById(id);
        return opt.orElse(null);
    }

    // Update feedback rating and review text
    public void updateFeedback(Long feedbackId, int newOverallRating, String newReview) {
        FeedbackModel feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) {
            return;
        }

        feedback.setOverall_rating(newOverallRating);
        feedback.setReview(newReview);

        feedback = feedbackRepository.save(feedback);
        recalculateRestaurantRating(feedback.getRestaurant());
    }

    public void deleteFeedback(Long feedbackId, UserModel user) {
        FeedbackModel feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) return;

        feedbackRepository.deleteById(feedbackId);
        recalculateRestaurantRating(feedback.getRestaurant());
    }

    public void deleteFeedbackByAdmin(Long feedbackId) {
        FeedbackModel feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) return;

        RestaurantModel restaurant = feedback.getRestaurant();
        feedbackRepository.deleteById(feedbackId);
        recalculateRestaurantRating(restaurant);
    }

    private void recalculateRestaurantRating(RestaurantModel restaurant) {
        List<FeedbackModel> feedbacks = feedbackRepository.findByRestaurant(restaurant);
        if (feedbacks == null || feedbacks.isEmpty()) {
            restaurant.setOverallRating(0.0);
            restaurantRepository.save(restaurant);
            return;
        }

        int sum = 0;
        for (FeedbackModel feedback : feedbacks) {
            sum += feedback.getOverall_rating();
        }

        double average = sum / (double) feedbacks.size();
        average = Math.max(0.0, Math.min(5.0, average));
        restaurant.setOverallRating(Math.round(average * 10.0) / 10.0);
        restaurantRepository.save(restaurant);
    }

    /**
     * Delete all feedback for a restaurant (used when owner account is deactivated).
     */
    public void deleteFeedbackByRestaurant(RestaurantModel restaurant) {
        List<FeedbackModel> feedbacks = feedbackRepository.findByRestaurant(restaurant);
        if (feedbacks != null && !feedbacks.isEmpty()) {
            feedbackRepository.deleteAll(feedbacks);
        }
    }
}
