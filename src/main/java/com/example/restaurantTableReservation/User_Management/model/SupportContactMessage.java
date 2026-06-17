package com.example.restaurantTableReservation.User_Management.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_contact_messages")
public class SupportContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String category;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(name = "message_source", nullable = false, length = 40)
    private String messageSource;

    @Column(name = "attempted_login_email", length = 255)
    private String attemptedLoginEmail;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "restaurant_name", length = 255)
    private String restaurantName;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "admin_read", nullable = false)
    private boolean adminRead;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(String messageSource) {
        this.messageSource = messageSource;
    }

    public String getAttemptedLoginEmail() {
        return attemptedLoginEmail;
    }

    public void setAttemptedLoginEmail(String attemptedLoginEmail) {
        this.attemptedLoginEmail = attemptedLoginEmail;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public boolean isAdminRead() {
        return adminRead;
    }

    public void setAdminRead(boolean adminRead) {
        this.adminRead = adminRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
