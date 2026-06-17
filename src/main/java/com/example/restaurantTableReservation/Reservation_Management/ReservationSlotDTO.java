package com.example.restaurantTableReservation.Reservation_Management;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationSlotDTO {

    private LocalTime startingTime;
    private LocalTime endingTime;
    private LocalDate date;

    private Integer noOfGuest;
    private Integer requestedDuration;
    private Long customerId;

    private Long restaurantId;

    public LocalTime getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(LocalTime endingTime) {
        this.endingTime = endingTime;
    }

    public LocalTime getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(LocalTime startingTime) {
        this.startingTime = startingTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getNoOfGuest() {
        return noOfGuest;
    }

    public void setNoOfGuest(Integer noOfGuest) {
        this.noOfGuest = noOfGuest;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Integer getRequestedDuration() {
        return requestedDuration;
    }

    public void setRequestedDuration(Integer requestedDuration) {
        this.requestedDuration = requestedDuration;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }


    //automatically inherits from the built-in Object class
    @Override
    public String toString() {
        return "ReservationSlotDTO{" +
                "startingTime=" + startingTime +
                ", endingTime=" + endingTime +
                ", date=" + date +
                ", noOfGuest=" + noOfGuest +
                ", requestedDuration=" + requestedDuration +
                ", customerId=" + customerId +
                ", restaurantId=" + restaurantId +
                '}';
    }
}