package com.example.restaurantTableReservation.Admin_Management.repository; // Package: Spring Data repositories for admin

/*
 * Spring Data JPA repository for Admin entities.
 * Spring generates SQL automatically from method names.
 */

import com.example.restaurantTableReservation.Admin_Management.model.Admin; // Entity type
import org.springframework.data.jpa.repository.JpaRepository; // Base interface with CRUD methods

// Interface only — Spring creates implementation at runtime
public interface AdminRepository extends JpaRepository<Admin, Long> {
    // JpaRepository<Admin, Long> means: entity Admin, primary key type Long
    // Inherited: save(), findAll(), findById(), deleteById(), count(), etc.

    Admin findByEmail(String email); // SELECT * FROM admins WHERE email = ?

    Admin findByEmailIgnoreCase(String email); // Same but case-insensitive (used at login)
}
