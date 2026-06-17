package com.example.restaurantTableReservation.Feedback_Management;

public class FeedbackDto {
    private Long restaurantId;
    private Long customerId;
    private int overallRating;
    private int foodCuisineRating;
    private int staffServiceRating;
    private int ambienceAtmosphereRating;
    private int valueForMoneyRating;
    private String review;


    //getter and setters
    public Long getRestaurantId() {
        return restaurantId;
    }
    // Set the restaurant ID associated with the feedback
    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    // Retrieve the customer ID who submitted the feedback
    public Long getCustomerId() {
        return customerId;
    }


    // Set the customer ID for the feedback
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }


    // Retrieve the overall rating given by the customer
    public int getOverallRating() {
        return overallRating;
    }


    // Set the overall rating for the feedback
    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }


    // Retrieve the food and cuisine rating
    public int getFoodCuisineRating() {
        return foodCuisineRating;
    }


    // Set the food and cuisine rating
    public void setFoodCuisineRating(int foodCuisineRating) {
        this.foodCuisineRating = foodCuisineRating;
    }


    // Retrieve the staff service rating
    public int getStaffServiceRating() {
        return staffServiceRating;
    }


    // Set the staff service rating
    public void setStaffServiceRating(int staffServiceRating) {
        this.staffServiceRating = staffServiceRating;
    }


    // Retrieve the ambience and atmosphere rating
    public int getAmbienceAtmosphereRating() {
        return ambienceAtmosphereRating;
    }


    // Set the ambience and atmosphere rating
    public void setAmbienceAtmosphereRating(int ambienceAtmosphereRating) {
        this.ambienceAtmosphereRating = ambienceAtmosphereRating;
    }

    // Retrieve the value-for-money rating
    public int getValueForMoneyRating() {
        return valueForMoneyRating;
    }


    // Set the value-for-money rating
    public void setValueForMoneyRating(int valueForMoneyRating) {
        this.valueForMoneyRating = valueForMoneyRating;
    }

    // Retrieve the customer review text
    public String getReview() {
        return review;
    }


    // Set the customer review text
    public void setReview(String review) {
        this.review = review;
    }


    // Return a formatted string representation of the feedback data for debugging and logging purposes
    @Override
    public String toString() {
        return "FeedbackDto{" +
                "restaurantId=" + restaurantId +
                ", customerId=" + customerId +
                ", overallRating=" + overallRating +
                ", foodCuisineRating=" + foodCuisineRating +
                ", staffServiceRating=" + staffServiceRating +
                ", ambienceAtmosphereRating=" + ambienceAtmosphereRating +
                ", valueForMoneyRating=" + valueForMoneyRating +
                ", review='" + review + '\'' +
                '}';
    }
}
