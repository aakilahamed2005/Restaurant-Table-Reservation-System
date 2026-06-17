package com.example.restaurantTableReservation.User_Management.dto;

public class DeleteDto {
    private String password;
    private String checkingString;

    public String getCheckingString() {
        return checkingString;
    }

    public void setCheckingString(String checkingString) {
        this.checkingString = checkingString;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
