package com.example.restaurantTableReservation.Admin_Management.dto;

import java.time.LocalDateTime;

/*
 * DTO for admin notification bell JSON API (SupportMessageAdminView).
 * Maps SupportContactMessage entity fields for display in notification-bell fragment.
 * category/description: user message; messageSource: CUSTOMER, OWNER, LOGIN, etc.
 * restaurantId/restaurantName/ownerUserId: populated for owner-related tickets.
 * adminRead: whether admin has opened the message; createdAt for sorting.
 */
public class SupportMessageAdminView {

    private Long id;
    private String category;
    private String email;
    private String description;
    private String messageSource;
    private String attemptedLoginEmail;
    private Long restaurantId;
    private String restaurantName;
    private Long ownerUserId;
    private boolean adminRead;
    private LocalDateTime createdAt;

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
