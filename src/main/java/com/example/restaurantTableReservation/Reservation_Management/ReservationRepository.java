package com.example.restaurantTableReservation.Reservation_Management;

import com.example.restaurantTableReservation.User_Management.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationModel, Long>{

    //custom methods
    List<ReservationModel> findByRestaurantId(Long id);
    List<ReservationModel> findByRestaurantIdAndDateAndStatus(Long id, LocalDate date, Status status);

    List<ReservationModel> findByCustomerId(Long customerId);
    //to fetch user's data from DB

    ReservationModel findByIdAndCustomerId(Long reservationId, Long customerId);

    void deleteByCustomer(UserModel user);

    List<ReservationModel> findByCustomerIdAndStatus(Long customerId, Status status);
}
