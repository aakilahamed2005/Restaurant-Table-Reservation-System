package com.example.restaurantTableReservation.Restaurant_Mangement.model;


import com.example.restaurantTableReservation.Restaurant_Mangement.Status;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.Seat_Management.model.SeatModel;
import jakarta.persistence.*;
import java.util.List;

/**
 * JPA entity representing a restaurant in the system.
 * <p>
 * Maps to the `restaurants` table and contains descriptive fields,
 * relationships to the owning `UserModel`, seat information and
 * related images/schedules.
 */
@Entity
@Table(name = "restaurants")
public class RestaurantModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Primary key (auto-generated)

    // Relationship with `UserModel` - owner of this restaurant
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserModel owner;

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    // Short human-readable name for the restaurant

    @Column(name = "short_description")
    private String shortDescription;

    // Brief summary shown in listings

    @Column(columnDefinition = "TEXT",name = "description")
    private String description;

    // Longer, HTML-capable description stored as TEXT

    @Column(name = "cuisine")
    private String cuisine;

    // Cuisine type (e.g., "Italian", "Japanese")

    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "province")
    private String province;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Contact details

    // Legacy columns are kept for DB compatibility. The canonical values
    // (total seats and max party size) are stored on the related `SeatModel`.
    @Column(name = "total_seats", nullable = false)
    private int legacyTotalSeats;

    @Column(name = "max_party_size", nullable = false)
    private int legacyMaxPartySize;

    @Column(name = "price_range")
    private String priceRange;

    // Example values: "$", "$$", "$$$"

    @Column(name = "approved")
    private boolean approved = false;

    // Flag indicating whether an admin has approved this restaurant

    // Persist enum as STRING into a VARCHAR column to avoid MySQL ENUM mismatch/truncation
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private Status status = Status.PENDING;

    // Lifecycle/status of this restaurant (PENDING, ACTIVE, SUSPENDED, etc.)

    @Column(name = "overall_rating")
    private double overallRating = 0;

    // Average rating computed from reviews (0..5)

    @Column(name = "website")
    private String website;

    @Column(name = "has_parking")
    private boolean hasParking;

    @Column(name = "has_wifi")
    private boolean hasWifi;

    @Column(name = "halal_certified")
    private boolean halalCertified;


    // One-to-one relationship to SeatModel which holds canonical seating info
    @OneToOne(mappedBy = "restaurant")
    private SeatModel seat;


    // Opening hours / schedule entries for this restaurant
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<RestaurantScheduleModel> schedules;

    // Images uploaded for the restaurant (owner-provided)
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantImageModel> images;

    // Default Constructor
    public RestaurantModel() {
    }

    // Getters and Setters


    public boolean isHalalCertified() {
        return halalCertified;
    }

    public void setHalalCertified(boolean halalCertified) {
        this.halalCertified = halalCertified;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public SeatModel getSeat() {
        return seat;
    }

    public void setSeat(SeatModel seat) {
        // Attach the SeatModel and keep the bidirectional link consistent.
        // Also update legacy fields so older DB views remain consistent.
        this.seat = seat;
        if (seat != null && seat.getRestaurant() != this) {
            seat.setRestaurant(this);
        }
        if (seat != null) {
            this.legacyTotalSeats = seat.getTotalSeats();
            this.legacyMaxPartySize = seat.getMaxPartySize();
        }
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public List<RestaurantScheduleModel> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<RestaurantScheduleModel> schedules) {
        this.schedules = schedules;
    }

    public List<RestaurantImageModel> getImages() {
        return images;
    }

    public void setImages(List<RestaurantImageModel> images) {
        this.images = images;
    }

    public int getMaxPartySize() {
        // Prefer the canonical value from SeatModel if available,
        // otherwise fall back to the legacy column.
        return seat != null ? seat.getMaxPartySize() : legacyMaxPartySize;
    }

    public void setMaxPartySize(int maxPartySize) {
        // Update both legacy storage and SeatModel (if present)
        this.legacyMaxPartySize = maxPartySize;
        if (this.seat != null) {
            this.seat.setMaxPartySize(maxPartySize);
        }
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserModel getOwner() {
        return owner;
    }

    public void setOwner(UserModel owner) {
        this.owner = owner;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String address) {
        this.streetAddress = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public int getTotalSeats() {
        // Prefer canonical SeatModel value, fall back to legacy DB column.
        return seat != null ? seat.getTotalSeats() : legacyTotalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        // Keep legacy and canonical storage in sync when possible.
        this.legacyTotalSeats = totalSeats;
        if (this.seat != null) {
            this.seat.setTotalSeats(totalSeats);
        }
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }



    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public double getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(double overallRating) {
        this.overallRating = overallRating;
    }
}

