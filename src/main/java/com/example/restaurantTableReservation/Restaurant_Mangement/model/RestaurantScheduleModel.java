package com.example.restaurantTableReservation.Restaurant_Mangement.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a single day's opening hours for a `RestaurantModel`.
 * <p>
 * Stored in the `restaurant_schedule` table with the day of week,
 * open/close times and whether the restaurant is closed that day.
 */
@Entity
@Table(name = "restaurant_schedule")
public class RestaurantScheduleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Primary key for schedule entry

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    // Day of week this schedule applies to (MONDAY..SUNDAY)

    private LocalTime openTime;

    // Opening time (local time) for the day

    private LocalTime closeTime;

    // Closing time (local time) for the day

    private boolean closed;

    // If true, the restaurant is closed the whole day and open/close
    // times should be ignored.

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private RestaurantModel restaurant;

    // Owning restaurant for this schedule entry (many schedules -> one restaurant)

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public RestaurantModel getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantModel restaurant) {
        this.restaurant = restaurant;
    }
}
