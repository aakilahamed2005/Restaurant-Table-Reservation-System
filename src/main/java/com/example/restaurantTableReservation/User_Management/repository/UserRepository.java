package com.example.restaurantTableReservation.User_Management.repository;


import com.example.restaurantTableReservation.User_Management.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findByPhoneNumber(String phoneNumber);
    Optional<UserModel> findByEmailIgnoreCase(String email);
}
