package com.example.restaurantTableReservation.Restaurant_Mangement.dto;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

/**
 * DTO used to submit or edit restaurant registration details from the owner-facing form.
 * Fields include basic metadata, contact/location, capacity, amenities and weekly schedules.
 */
public class RestaurantRegisterDto {

    // Basic information about the restaurant
    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Restaurant name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Cuisine is required")
    private String cuisine;

    @NotBlank(message = "Price range is required")
    private String priceRange;

    @Size(max = 120, message = "Short description must be at most 120 characters")
    private String shortDescription;

    @NotBlank(message = "Description is required")
    private String description;


    // Location and contact information
    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Province is required")
    private String province;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+94\\d{9}$|^0\\d{9}$", message = "Phone number must be +94 followed by 9 digits, or start with 0 followed by 9 digits")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    private String website;


    // Capacity, party size and weekly opening hours
    @Min(value = 1, message = "Seating capacity must be at least 1")
    @Max(value = 500, message = "Seating capacity must be 500 or less")
    private int seatingCapacity;
    private int maxPartySize;


    // Features and amenities flags
    private boolean hasParking;
    private boolean hasWifi;
    private boolean halalCertified;


    // Monday schedule fields
    private boolean mondayOpen;
    private LocalTime mondayOpeningTime;
    private LocalTime mondayClosingTime;

    // Tuesday schedule fields
    private boolean tuesdayOpen;
    private LocalTime tuesdayOpeningTime;
    private LocalTime tuesdayClosingTime;


    // Wednesday schedule fields
    private boolean wednesdayOpen;
    private LocalTime wednesdayOpeningTime;
    private LocalTime wednesdayClosingTime;


    // Thursday schedule fields
    private boolean thursdayOpen;
    private LocalTime thursdayOpeningTime;
    private LocalTime thursdayClosingTime;


    // Friday schedule fields
    private boolean fridayOpen;
    private LocalTime fridayOpeningTime;
    private LocalTime fridayClosingTime;


    // Saturday schedule fields
    private boolean saturdayOpen;
    private LocalTime saturdayOpeningTime;
    private LocalTime saturdayClosingTime;


    // Sunday schedule fields
    private boolean sundayOpen;
    private LocalTime sundayOpeningTime;
    private LocalTime sundayClosingTime;


    public LocalTime getFridayClosingTime() {
        return fridayClosingTime;
    }

    public void setFridayClosingTime(LocalTime fridayClosingTime) {
        this.fridayClosingTime = fridayClosingTime;
    }

    public boolean isFridayOpen() {
        return fridayOpen;
    }

    public void setFridayOpen(boolean fridayOpen) {
        this.fridayOpen = fridayOpen;
    }

    public LocalTime getFridayOpeningTime() {
        return fridayOpeningTime;
    }

    public void setFridayOpeningTime(LocalTime fridayOpeningTime) {
        this.fridayOpeningTime = fridayOpeningTime;
    }

    public LocalTime getMondayClosingTime() {
        return mondayClosingTime;
    }

    public void setMondayClosingTime(LocalTime mondayClosingTime) {
        this.mondayClosingTime = mondayClosingTime;
    }

    public boolean isMondayOpen() {
        return mondayOpen;
    }

    public void setMondayOpen(boolean mondayOpen) {
        this.mondayOpen = mondayOpen;
    }

    public LocalTime getMondayOpeningTime() {
        return mondayOpeningTime;
    }

    public void setMondayOpeningTime(LocalTime mondayOpeningTime) {
        this.mondayOpeningTime = mondayOpeningTime;
    }

    public LocalTime getSaturdayClosingTime() {
        return saturdayClosingTime;
    }

    public void setSaturdayClosingTime(LocalTime saturdayClosingTime) {
        this.saturdayClosingTime = saturdayClosingTime;
    }

    public boolean isSaturdayOpen() {
        return saturdayOpen;
    }

    public void setSaturdayOpen(boolean saturdayOpen) {
        this.saturdayOpen = saturdayOpen;
    }

    public LocalTime getSaturdayOpeningTime() {
        return saturdayOpeningTime;
    }

    public void setSaturdayOpeningTime(LocalTime saturdayOpeningTime) {
        this.saturdayOpeningTime = saturdayOpeningTime;
    }

    public LocalTime getSundayClosingTime() {
        return sundayClosingTime;
    }

    public void setSundayClosingTime(LocalTime sundayClosingTime) {
        this.sundayClosingTime = sundayClosingTime;
    }

    public boolean isSundayOpen() {
        return sundayOpen;
    }

    public void setSundayOpen(boolean sundayOpen) {
        this.sundayOpen = sundayOpen;
    }

    public LocalTime getSundayOpeningTime() {
        return sundayOpeningTime;
    }

    public void setSundayOpeningTime(LocalTime sundayOpeningTime) {
        this.sundayOpeningTime = sundayOpeningTime;
    }

    public LocalTime getThursdayClosingTime() {
        return thursdayClosingTime;
    }

    public void setThursdayClosingTime(LocalTime thursdayClosingTime) {
        this.thursdayClosingTime = thursdayClosingTime;
    }

    public boolean isThursdayOpen() {
        return thursdayOpen;
    }

    public void setThursdayOpen(boolean thursdayOpen) {
        this.thursdayOpen = thursdayOpen;
    }

    public LocalTime getThursdayOpeningTime() {
        return thursdayOpeningTime;
    }

    public void setThursdayOpeningTime(LocalTime thursdayOpeningTime) {
        this.thursdayOpeningTime = thursdayOpeningTime;
    }

    public LocalTime getTuesdayClosingTime() {
        return tuesdayClosingTime;
    }

    public void setTuesdayClosingTime(LocalTime tuesdayClosingTime) {
        this.tuesdayClosingTime = tuesdayClosingTime;
    }

    public boolean isTuesdayOpen() {
        return tuesdayOpen;
    }

    public void setTuesdayOpen(boolean tuesdayOpen) {
        this.tuesdayOpen = tuesdayOpen;
    }

    public LocalTime getTuesdayOpeningTime() {
        return tuesdayOpeningTime;
    }

    public void setTuesdayOpeningTime(LocalTime tuesdayOpeningTime) {
        this.tuesdayOpeningTime = tuesdayOpeningTime;
    }

    public LocalTime getWednesdayClosingTime() {
        return wednesdayClosingTime;
    }

    public void setWednesdayClosingTime(LocalTime wednesdayClosingTime) {
        this.wednesdayClosingTime = wednesdayClosingTime;
    }

    public boolean isWednesdayOpen() {
        return wednesdayOpen;
    }

    public void setWednesdayOpen(boolean wednesdayOpen) {
        this.wednesdayOpen = wednesdayOpen;
    }

    public LocalTime getWednesdayOpeningTime() {
        return wednesdayOpeningTime;
    }

    public void setWednesdayOpeningTime(LocalTime wednesdayOpeningTime) {
        this.wednesdayOpeningTime = wednesdayOpeningTime;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public boolean isHalalCertified() {
        return halalCertified;
    }

    public void setHalalCertified(boolean halalCertified) {
        this.halalCertified = halalCertified;
    }

    public int getMaxPartySize() {
        return maxPartySize;
    }

    public void setMaxPartySize(int maxPartySize) {
        this.maxPartySize = maxPartySize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }



    @Override
    public String toString() {
        return "RestaurantRegisterDto{" +
                "name='" + name + '\'' +
                ", cuisine='" + cuisine + '\'' +
                ", priceRange='" + priceRange + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", description='" + description + '\'' +

                ", streetAddress='" + streetAddress + '\'' +
                ", city='" + city + '\'' +
                ", province='" + province + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", website='" + website + '\'' +

                ", seatingCapacity=" + seatingCapacity +
                ", maxPartySize=" + maxPartySize +

                ", hasParking=" + hasParking +
                ", hasWifi=" + hasWifi +
                ", halalCertified=" + halalCertified +

                ", mondayOpen=" + mondayOpen +
                ", mondayOpeningTime=" + mondayOpeningTime +
                ", mondayClosingTime=" + mondayClosingTime +

                ", tuesdayOpen=" + tuesdayOpen +
                ", tuesdayOpeningTime=" + tuesdayOpeningTime +
                ", tuesdayClosingTime=" + tuesdayClosingTime +

                ", wednesdayOpen=" + wednesdayOpen +
                ", wednesdayOpeningTime=" + wednesdayOpeningTime +
                ", wednesdayClosingTime=" + wednesdayClosingTime +

                ", thursdayOpen=" + thursdayOpen +
                ", thursdayOpeningTime=" + thursdayOpeningTime +
                ", thursdayClosingTime=" + thursdayClosingTime +

                ", fridayOpen=" + fridayOpen +
                ", fridayOpeningTime=" + fridayOpeningTime +
                ", fridayClosingTime=" + fridayClosingTime +

                ", saturdayOpen=" + saturdayOpen +
                ", saturdayOpeningTime=" + saturdayOpeningTime +
                ", saturdayClosingTime=" + saturdayClosingTime +

                ", sundayOpen=" + sundayOpen +
                ", sundayOpeningTime=" + sundayOpeningTime +
                ", sundayClosingTime=" + sundayClosingTime +
                '}';
    }
}
