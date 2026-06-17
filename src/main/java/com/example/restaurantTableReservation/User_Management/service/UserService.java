package com.example.restaurantTableReservation.User_Management.service;

import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.repository.UserRepository;
import com.example.restaurantTableReservation.User_Management.dto.PasswordChangeDto;
import com.example.restaurantTableReservation.User_Management.dto.UpdateDto;
import com.example.restaurantTableReservation.Reservation_Management.ReservationRepository;
import com.example.restaurantTableReservation.Reservation_Management.Status;
import com.example.restaurantTableReservation.Feedback_Management.FeedbackRepository;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    FeedbackRepository feedbackRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

    public boolean isPhoneNumberUsedByAnotherUser(String phoneNumber, Long userId) {
        return userRepository.existsByPhoneNumberAndIdNot(phoneNumber, userId);
    }

    public UserModel update(UpdateDto updateDto, UserModel user) {
        user.setFirstName(updateDto.getFirstName());
        user.setLastName(updateDto.getLastName());
        user.setEmail(updateDto.getEmail());
        user.setPhoneNumber(updateDto.getPhoneNumber());

        return userRepository.save(user);
    }

    public void changePassword(UserModel user, PasswordChangeDto passwordChangeDto){

        if (!BCrypt.checkpw(
                passwordChangeDto.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException("Incorrect Password");

        } else {

            user.setPassword(
                    BCrypt.hashpw(
                            passwordChangeDto.getNewPassword(),
                            BCrypt.gensalt()
                    )
            );

            userRepository.save(user);
        }
    }

    // Get total number of bookings for a customer
    public long getTotalBookingsForCustomer(Long customerId) {
        return reservationRepository.findByCustomerId(customerId).size();
    }

    // Get number of upcoming bookings for a customer
    public long getUpcomingBookingsForCustomer(Long customerId) {
        List<com.example.restaurantTableReservation.Reservation_Management.ReservationModel> confirmedReservations =
            reservationRepository.findByCustomerIdAndStatus(customerId, Status.CONFIRMED);

        LocalDate today = LocalDate.now();
        long upcomingCount = 0;

        for(com.example.restaurantTableReservation.Reservation_Management.ReservationModel reservation : confirmedReservations) {
            if(reservation.getDate() != null && !reservation.getDate().isBefore(today)) {
                upcomingCount++;
            }
        }

        return upcomingCount;
    }

    // Get average review rating for a customer
    public Double getAverageReviewRatingForCustomer(Long customerId) {
        List<com.example.restaurantTableReservation.Feedback_Management.FeedbackModel> feedbacks =
            feedbackRepository.findByCustomer_Id(customerId);

        if(feedbacks == null || feedbacks.isEmpty()) {
            return 0.0;
        }

        int totalRating = 0;
        for(com.example.restaurantTableReservation.Feedback_Management.FeedbackModel feedback : feedbacks) {
            totalRating += feedback.getOverall_rating();
        }

        double average = (double) totalRating / feedbacks.size();
        return Math.round(average * 10.0) / 10.0;
    }

    // Get total number of restaurants for an owner
    public long getTotalRestaurantsForOwner(Long ownerId) {
        UserModel owner = userRepository.findById(ownerId).orElse(null);
        if(owner == null) {
            return 0;
        }
        return restaurantRepository.findByOwner(owner).size();
    }

    // Get average rating for all restaurants owned by an owner
    public Double getAverageRatingForOwner(Long ownerId) {
        UserModel owner = userRepository.findById(ownerId).orElse(null);
        if(owner == null) {
            return 0.0;
        }

        List<com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel> restaurants =
            restaurantRepository.findByOwner(owner);

        if(restaurants == null || restaurants.isEmpty()) {
            return 0.0;
        }

        double totalRating = 0;
        for(com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel restaurant : restaurants) {
            totalRating += restaurant.getOverallRating();
        }

        double average = totalRating / restaurants.size();
        return Math.round(average * 10.0) / 10.0;
    }
}
