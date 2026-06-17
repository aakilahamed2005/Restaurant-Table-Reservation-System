package com.example.restaurantTableReservation.Admin_Management.model; // Package: admin database entity classes

/*
 * JPA entity for platform administrators (table: admins).
 * After login, stored in HTTP session as "loggedAdmin".
 */

import jakarta.persistence.*; // JPA: @Entity, @Table, @Id, @Column, etc.

@Entity // Marks this class as a JPA entity (maps to a database table)
@Table(name = "admins") // Database table name is "admins"
public class Admin {

    @Id // This field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL auto-increment for new rows
    private Long adminId; // Unique ID for each admin row

    private String adminName; // Display name (shown in sidebar footer)

    @Column(unique = true) // Email must be unique across all admins
    private String email; // Login username (stored lowercase after register)

    private String password; // BCrypt hash only — never store plain text password

    private String role; // "SUPER_ADMIN" (full access) or "ADMIN" (limited)

    public Admin() { // Required empty constructor for JPA/Hibernate
    }

    // Convenience constructor when creating a new admin in code
    public Admin(String adminName, String email, String password, String role) {
        this.adminName = adminName; // Set display name
        this.email = email; // Set email
        this.password = password; // Plain password here; service will hash before save
        this.role = role; // Set role string
    }

    public Long getAdminId() { // Read primary key
        return adminId;
    }

    public void setAdminId(Long adminId) { // Write primary key (rarely used)
        this.adminId = adminId;
    }

    public String getAdminName() { // Read display name
        return adminName;
    }

    public void setAdminName(String adminName) { // Update display name on edit form
        this.adminName = adminName;
    }

    public String getEmail() { // Read login email
        return email;
    }

    public void setEmail(String email) { // Update email on edit form
        this.email = email;
    }

    public String getPassword() { // Read hashed password (used only for BCrypt check at login)
        return password;
    }

    public void setPassword(String password) { // Set password (plain or hash depending on caller)
        this.password = password;
    }

    public String getRole() { // Read role for permission checks in controllers
        return role;
    }

    public void setRole(String role) { // Set role (only super admin should change this)
        this.role = role;
    }
}
