package com.example.restaurantTableReservation.Seat_Management.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class SeatCheckingForm {
    private Long restaurantId;
    private Long customerId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    private int noOfGuests;
    private int duration;

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getNoOfGuests() {
        return noOfGuests;
    }

    public void setNoOfGuests(int noOfGuests) {
        this.noOfGuests = noOfGuests;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "SeatCheckingForm{" +
                "restaurantId=" + restaurantId +
                ", customerId=" + customerId +
                ", date=" + date +
                ", noOfGuests=" + noOfGuests +
                ", duration=" + duration +
                '}';
    }
}
