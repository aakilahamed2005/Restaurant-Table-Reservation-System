package com.example.restaurantTableReservation.User_Management.repository;

import com.example.restaurantTableReservation.User_Management.model.SupportContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportContactMessageRepository extends JpaRepository<SupportContactMessage, Long> {
    List<SupportContactMessage> findAllByOrderByCreatedAtDesc();
    List<SupportContactMessage> findAllByAdminReadFalse();
    long countByAdminReadFalse();
}
