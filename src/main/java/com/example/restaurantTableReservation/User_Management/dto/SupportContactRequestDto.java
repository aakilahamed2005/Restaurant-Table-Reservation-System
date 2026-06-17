package com.example.restaurantTableReservation.User_Management.dto;


public class SupportContactRequestDto {

    private String category;
    private String email;
    private String description;


    private String attemptedLoginEmail;

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

    public String getAttemptedLoginEmail() {
        return attemptedLoginEmail;
    }

    public void setAttemptedLoginEmail(String attemptedLoginEmail) {
        this.attemptedLoginEmail = attemptedLoginEmail;
    }
}
