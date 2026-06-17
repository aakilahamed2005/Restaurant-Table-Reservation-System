package com.example.restaurantTableReservation.Feedback_Management;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
public class FeedbackModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private UserModel customer;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantModel restaurant;

    @Column(nullable = false)
    private int overall_rating;

    @Column(nullable = false)
    private int food_cuisine_rating;

    @Column(nullable = false)
    private int staff_service_rating;

    @Column(nullable = false)
    private int ambience_atmosphere_rating;

    @Column(nullable = false)
    private int value_for_money_rating;

    @Column(columnDefinition = "TEXT")
    private String review;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Getters and Setters


    // Returns the restaurant associated with this feedback
    public RestaurantModel getRestaurant() {
        return restaurant;
    }

    // Sets the restaurant associated with this feedback
    public void setRestaurant(RestaurantModel restaurant) {
        this.restaurant = restaurant;
    }

    // Returns the customer who submitted the feedback
    public UserModel getCustomer() {
        return customer;
    }

    // Sets the customer who submitted the feedback
    public void setCustomer(UserModel customer) {
        this.customer = customer;
    }

    // Returns the staff service rating value
    public int getStaff_service_rating() {
        return staff_service_rating;
    }

    // Sets the staff service rating value
    public void setStaff_service_rating(int staff_service_rating) {
        this.staff_service_rating = staff_service_rating;
    }

    // Returns the food and cuisine rating value
    public int getFood_cuisine_rating() {
        return food_cuisine_rating;
    }

    // Sets the food and cuisine rating value
    public void setFood_cuisine_rating(int food_cuisine_rating) {
        this.food_cuisine_rating = food_cuisine_rating;
    }


    // Returns the ambience and atmosphere rating value
    public int getAmbience_atmosphere_rating() {
        return ambience_atmosphere_rating;
    }

    // Sets the ambience and atmosphere rating value
    public void setAmbience_atmosphere_rating(int ambience_atmosphere_rating) {
        this.ambience_atmosphere_rating = ambience_atmosphere_rating;
    }

    // Returns the ambience and atmosphere rating value
    public int getValue_for_money_rating() {
        return value_for_money_rating;
    }

    // Sets the value for money rating value
    public void setValue_for_money_rating(int value_for_money_rating) {
        this.value_for_money_rating = value_for_money_rating;
    }

    // Returns the feedback ID
    public Long getId() { return id; }

    // Sets the feedback ID
    public void setId(Long id) { this.id = id; }

    // Returns the overall rating value
    public int getOverall_rating() { return overall_rating; }


    // Sets the overall rating value
    public void setOverall_rating(int overall_rating) { this.overall_rating = overall_rating; }


    // Returns the written review comment
    public String getReview() { return review; }

    // Sets the written review comment
    public void setReview(String review) { this.review = review; }

    // Returns the date and time when the feedback was created
    public LocalDateTime getCreatedAt() { return createdAt; }


    // Sets the feedback creation date and time
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
