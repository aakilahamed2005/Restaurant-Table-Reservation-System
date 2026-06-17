package com.example.restaurantTableReservation.Seat_Management.dto;

import java.time.LocalTime;

public class TimeSlotDTO {

    private static final int LOW_SEATS_THRESHOLD = 5;

    private LocalTime startTime;
    private LocalTime endTime;
    private int seatsAvailable;
    private boolean available;
    private boolean pastSlot;

    public TimeSlotDTO(LocalTime startTime, LocalTime endTime,
                       int seatsAvailable, boolean available, boolean pastSlot) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.seatsAvailable = Math.max(seatsAvailable, 0);
        this.available = available;
        this.pastSlot = pastSlot;
    }

    /** Low availability while the slot is still bookable (≤ 5 seats left). */
    public boolean isLowSeats() {
        return available && seatsAvailable > 0 && seatsAvailable <= LOW_SEATS_THRESHOLD;
    }

    public boolean isPlentifulSeats() {
        return available && seatsAvailable > LOW_SEATS_THRESHOLD;
    }

    public boolean isFullyBooked() {
        return !pastSlot && seatsAvailable <= 0;
    }

    public String getStatusTag() {
        if (pastSlot) {
            return "Past";
        }
        if (seatsAvailable <= 0) {
            return "Full";
        }
        if (available && seatsAvailable <= LOW_SEATS_THRESHOLD) {
            return "Low seats";
        }
        if (available) {
            return "Available";
        }
        if (seatsAvailable <= LOW_SEATS_THRESHOLD) {
            return "Low seats";
        }
        return "Unavailable";
    }

    public String getSeatsText() {
        if (pastSlot) {
            return "Time has passed";
        }
        if (seatsAvailable <= 0) {
            return "Fully booked";
        }
        if (!available) {
            return "Only " + seatsAvailable + " seats left — not enough for your party";
        }
        if (seatsAvailable <= LOW_SEATS_THRESHOLD) {
            return "Only " + seatsAvailable + " seats left!";
        }
        return seatsAvailable + " seats available";
    }

    public String getBarColor() {
        if (pastSlot || seatsAvailable <= 0 || !available) {
            return "#C0392B";
        }
        return seatsAvailable > LOW_SEATS_THRESHOLD ? "#7A8C6E" : "#D4A853";
    }

    public int getBarWidth(int totalSeats) {
        if (totalSeats <= 0 || seatsAvailable <= 0) {
            return 0;
        }
        return (seatsAvailable * 100) / totalSeats;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isPastSlot() {
        return pastSlot;
    }
}
