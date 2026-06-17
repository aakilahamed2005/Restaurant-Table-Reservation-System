package com.example.restaurantTableReservation.Restaurant_Mangement.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO used to capture restaurant listing filters submitted by users.
 * - `cuisines`: optional list of cuisine types to include
 * - `priceRange`: optional price range filter (e.g. "$", "$$", or sentinel like "any_range")
 * - `rating`: minimum overall rating to include
 */
public class RestaurantFilterForm {
    // Selected cuisine types; empty list means "no cuisine filter"
    private List<String> cuisines = new ArrayList<>();

    // Selected price range filter; null or a sentinel value means "any"
    private String priceRange;

    // Minimum rating filter (0.0 means no minimum)
    private double rating;

    // Getters & Setters
    public List<String> getCuisines() { return cuisines; }
    public void setCuisines(List<String> cuisines) { this.cuisines = cuisines; }

    public String getPriceRange() { return priceRange; }
    public void setPriceRange(String priceRange) { this.priceRange = priceRange; }

    public double getRating() {return rating;}
    public void setRating(double rating) {this.rating = rating;}
}

