package com.example.restaurantTableReservation.Restaurant_Mangement;

import com.example.restaurantTableReservation.Restaurant_Mangement.dto.RestaurantFilterForm;
import com.example.restaurantTableReservation.Restaurant_Mangement.dto.RestaurantRegisterDto;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantScheduleModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantRepository;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantScheduleRepository;
import com.example.restaurantTableReservation.Seat_Management.SeatService;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.Reservation_Management.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Service
public class RestaurantService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    // Repository for persisting and querying Restaurant entities

    @Autowired
    private RestaurantScheduleRepository restaurantScheduleRepository;

    // Repository for restaurant opening/closing schedules

    @Autowired
    private SeatService seatService;

    // Service that manages seat counts and related seat entities

    @Autowired
    private ReservationService reservationService;

    // Service used to cancel reservations when restaurant status changes

    @Autowired
    private RestaurantImageService restaurantImageService;

    // Service for saving and deleting restaurant images


    @Transactional
    public RestaurantModel register(RestaurantRegisterDto restaurantRegisterDto, UserModel user, List<MultipartFile> images) throws IOException {
        // Build new RestaurantModel from DTO and associate owner
        RestaurantModel restaurantModel = new RestaurantModel();
        restaurantModel.setRestaurantName(restaurantRegisterDto.getName());
        restaurantModel.setCuisine(restaurantRegisterDto.getCuisine());
        restaurantModel.setPriceRange(restaurantRegisterDto.getPriceRange());
        restaurantModel.setShortDescription(restaurantRegisterDto.getShortDescription());
        restaurantModel.setDescription(restaurantRegisterDto.getDescription());

        restaurantModel.setStreetAddress(restaurantRegisterDto.getStreetAddress());
        restaurantModel.setCity(restaurantRegisterDto.getCity());
        restaurantModel.setProvince(restaurantRegisterDto.getProvince());
        restaurantModel.setPhoneNumber(restaurantRegisterDto.getPhoneNumber());
        restaurantModel.setEmail(restaurantRegisterDto.getEmail());
        restaurantModel.setWebsite(restaurantRegisterDto.getWebsite());

        restaurantModel.setTotalSeats(restaurantRegisterDto.getSeatingCapacity());
        restaurantModel.setMaxPartySize(restaurantRegisterDto.getMaxPartySize());

        restaurantModel.setHasParking(restaurantRegisterDto.isHasParking());
        restaurantModel.setHasWifi(restaurantRegisterDto.isHasWifi());
        restaurantModel.setHalalCertified(restaurantRegisterDto.isHalalCertified());

        // New restaurants should start as PENDING review (owner-submitted)
        restaurantModel.setStatus(Status.PENDING);
        restaurantModel.setOwner(user);
        restaurantModel = restaurantRepository.save(restaurantModel);
        seatService.saveOrUpdateSeat(restaurantModel, restaurantRegisterDto.getSeatingCapacity(), restaurantRegisterDto.getMaxPartySize());

        // Create schedules for all 7 days
        createSchedule(restaurantModel, DayOfWeek.MONDAY, restaurantRegisterDto.isMondayOpen(),
                restaurantRegisterDto.getMondayOpeningTime(), restaurantRegisterDto.getMondayClosingTime());
        createSchedule(restaurantModel, DayOfWeek.TUESDAY, restaurantRegisterDto.isTuesdayOpen(),
                restaurantRegisterDto.getTuesdayOpeningTime(), restaurantRegisterDto.getTuesdayClosingTime());
        createSchedule(restaurantModel, DayOfWeek.WEDNESDAY, restaurantRegisterDto.isWednesdayOpen(),
                restaurantRegisterDto.getWednesdayOpeningTime(), restaurantRegisterDto.getWednesdayClosingTime());
        createSchedule(restaurantModel, DayOfWeek.THURSDAY, restaurantRegisterDto.isThursdayOpen(),
                restaurantRegisterDto.getThursdayOpeningTime(), restaurantRegisterDto.getThursdayClosingTime());
        createSchedule(restaurantModel, DayOfWeek.FRIDAY, restaurantRegisterDto.isFridayOpen(),
                restaurantRegisterDto.getFridayOpeningTime(), restaurantRegisterDto.getFridayClosingTime());
        createSchedule(restaurantModel, DayOfWeek.SATURDAY, restaurantRegisterDto.isSaturdayOpen(),
                restaurantRegisterDto.getSaturdayOpeningTime(), restaurantRegisterDto.getSaturdayClosingTime());
        createSchedule(restaurantModel, DayOfWeek.SUNDAY, restaurantRegisterDto.isSundayOpen(),
                restaurantRegisterDto.getSundayOpeningTime(), restaurantRegisterDto.getSundayClosingTime());

        // Persist uploaded images (if any) and return saved entity
        restaurantImageService.saveImages(restaurantModel, images);
        return restaurantModel;
    }

    // ✅ Helper method to avoid repetition (used in register)
    private void createSchedule(RestaurantModel restaurant, DayOfWeek day, boolean isOpen, LocalTime openTime, LocalTime closeTime) {
        // Create and persist a single day's schedule (open/closed and times)
        RestaurantScheduleModel schedule = new RestaurantScheduleModel();
        schedule.setRestaurant(restaurant);
        schedule.setDay(day);
        schedule.setClosed(!isOpen);
        schedule.setOpenTime(isOpen ? openTime : null);
        schedule.setCloseTime(isOpen ? closeTime : null);
        restaurantScheduleRepository.save(schedule);
    }

    @Transactional


    public void update(RestaurantRegisterDto dto, Long restaurantId, UserModel user,
                       List<MultipartFile> newImages, List<Long> deleteImageIds) throws IOException {

        // Create new restaurant entity
        // Load restaurant; throw if missing
        RestaurantModel restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Security check
        // Ensure the requester is the owner of the restaurant
        if (!restaurant.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Update basic scalar fields from DTO
        restaurant.setRestaurantName(dto.getName());
        restaurant.setCuisine(dto.getCuisine());
        restaurant.setPriceRange(dto.getPriceRange());
        restaurant.setShortDescription(dto.getShortDescription());
        restaurant.setDescription(dto.getDescription());
        restaurant.setStreetAddress(dto.getStreetAddress());
        restaurant.setCity(dto.getCity());
        restaurant.setProvince(dto.getProvince());
        restaurant.setPhoneNumber(dto.getPhoneNumber());
        restaurant.setEmail(dto.getEmail());
        restaurant.setWebsite(dto.getWebsite());
        restaurant.setTotalSeats(dto.getSeatingCapacity());
        restaurant.setMaxPartySize(dto.getMaxPartySize());
        restaurant.setHasParking(dto.isHasParking());
        restaurant.setHasWifi(dto.isHasWifi());
        restaurant.setHalalCertified(dto.isHalalCertified());

        // Persist changes and update seat info accordingly
        restaurant = restaurantRepository.save(restaurant);
        seatService.saveOrUpdateSeat(restaurant, dto.getSeatingCapacity(), dto.getMaxPartySize());

        // ✅ Update schedules for ALL 7 days
        updateSchedule(restaurant, DayOfWeek.MONDAY, dto.isMondayOpen(), dto.getMondayOpeningTime(), dto.getMondayClosingTime());
        updateSchedule(restaurant, DayOfWeek.TUESDAY, dto.isTuesdayOpen(), dto.getTuesdayOpeningTime(), dto.getTuesdayClosingTime());
        updateSchedule(restaurant, DayOfWeek.WEDNESDAY, dto.isWednesdayOpen(), dto.getWednesdayOpeningTime(), dto.getWednesdayClosingTime());
        updateSchedule(restaurant, DayOfWeek.THURSDAY, dto.isThursdayOpen(), dto.getThursdayOpeningTime(), dto.getThursdayClosingTime());
        updateSchedule(restaurant, DayOfWeek.FRIDAY, dto.isFridayOpen(), dto.getFridayOpeningTime(), dto.getFridayClosingTime());
        updateSchedule(restaurant, DayOfWeek.SATURDAY, dto.isSaturdayOpen(), dto.getSaturdayOpeningTime(), dto.getSaturdayClosingTime());
        updateSchedule(restaurant, DayOfWeek.SUNDAY, dto.isSundayOpen(), dto.getSundayOpeningTime(), dto.getSundayClosingTime());

        // Handle image deletions and new image uploads
        restaurantImageService.deleteImages(restaurant, deleteImageIds);
        restaurantImageService.saveImages(restaurant, newImages);
    }

    private void updateSchedule(RestaurantModel restaurant, DayOfWeek day, boolean isOpen, LocalTime openTime, LocalTime closeTime) {
        // Find existing schedule or create new one
        Optional<RestaurantScheduleModel> existing = restaurantScheduleRepository.findByRestaurantAndDay(restaurant, day);

        RestaurantScheduleModel schedule = existing.orElse(new RestaurantScheduleModel());
        schedule.setRestaurant(restaurant);
        schedule.setDay(day);
        schedule.setClosed(!isOpen);
        schedule.setOpenTime(isOpen ? openTime : null);
        schedule.setCloseTime(isOpen ? closeTime : null);

        restaurantScheduleRepository.save(schedule);
    }

    //**
           // * Filters restaurants based on user-selected criteria.
            //*
           // * Supported filters:
          //  * - Cuisine type
 //* - Minimum rating
 //* - Price range
 //*
       //  * Only restaurants with ACTIVE status are returned.
 //* Restaurants with PENDING or DEACTIVATED status are excluded.

          //  * param filterForm Object containing filter criteria.
 //* return List of matching active restaurants.


    public List<RestaurantModel> getAllRestaurantsOfOwner(UserModel user) {
        return restaurantRepository.findByOwner(user);
    }



    /**
     * Filters restaurants based on user-selected criteria.

     * Supported filters:
     * - Cuisine type
     * - Minimum rating
     * - Price range

     * Only restaurants with ACTIVE status are returned.
     * Restaurants with PENDING or DEACTIVATED status are excluded.
    * return List of matching active restaurants.
     */
    public List<RestaurantModel> filter(RestaurantFilterForm filterForm) {
        // Build JPA specifications from filter form and return only ACTIVE restaurants
        return restaurantRepository.findAll(
                Specification.where(RestaurantSpecification.hasCuisine(filterForm.getCuisines()))
                        .and(RestaurantSpecification.hasMinRating(filterForm.getRating()))
                        .and(RestaurantSpecification.hasPriceRange(filterForm.getPriceRange()))
        ).stream()
         .filter(restaurant -> restaurant.getStatus() == Status.ACTIVE)
         .toList();
    }



    public RestaurantModel getRestaurantForView(Long id, UserModel requester, boolean isAdmin) {
        RestaurantModel restaurant = getRestaurantById(id);
        if (restaurant == null) {
            return null;
        }

        // Allow viewing if restaurant is active or requester is admin
        if (restaurant.getStatus() == Status.ACTIVE || isAdmin) {
            return restaurant;
        }

        // Allow owner to view their own restaurants regardless of status
        if (requester != null && restaurant.getOwner() != null && restaurant.getOwner().getId().equals(requester.getId())) {
            return restaurant;
        }

        // Otherwise hide the restaurant
        return null;
    }

    /**
     * Suspends a restaurant and cancels all of its active customer reservations.
     */
    @Transactional
    public void suspendRestaurant(Long restaurantId) {
        RestaurantModel restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Cancel all active reservations for this restaurant with a clear message
        reservationService.cancelReservationsByRestaurant(
            restaurantId,
            "This restaurant has been suspended, so your reservation was automatically cancelled. We’re sorry for the inconvenience and hope you’ll book another restaurant soon."
        );

        // Mark restaurant as suspended and persist
        restaurant.setStatus(Status.SUSPENDED);
        restaurantRepository.save(restaurant);
    }

    /**
     * Soft deletes a restaurant by setting its status to DEACTIVATED.

     * This method:
     * 1. Checks whether the restaurant exists.
     * 2. Verifies that the logged-in user is the owner.
     * 3. Cancels all reservations for the restaurant.
     * 4. Changes the restaurant status to DEACTIVATED.
     * 5. Saves the updated restaurant.

     * The restaurant record remains in the database, preserving reservation
     * and feedback history.
     *
    * param restaurantId ID of the restaurant to remove.
    * param user The logged-in user.
    * return true if removal was successful; false otherwise.
     */

    public boolean removeRestaurant(Long restaurantId, UserModel user) {
        Optional<RestaurantModel> optionalRestaurant = restaurantRepository.findById(restaurantId);
        if (optionalRestaurant.isEmpty() || user == null) {
            return false;
        }

        RestaurantModel restaurant = optionalRestaurant.get();
        if (!restaurant.getOwner().getId().equals(user.getId())) {
            return false;
        }

        // Cancel all confirmed reservations before deactivating the restaurant so customers see cancellations.
        try {
            // Attempt to cancel reservations; ignore failures to avoid leaving owner data inconsistent
            reservationService.cancelReservationsByRestaurant(
                    restaurantId,
                    "This restaurant has been deactivated, so your reservation was automatically cancelled. We’re sorry for the inconvenience and hope you’ll book another restaurant soon."
            );
        } catch (Exception ignored) {
            // best-effort cancellation
        }

        // Soft delete by changing status; preserve images/history for auditing
        restaurant.setStatus(Status.DEACTIVATED);
        restaurantRepository.save(restaurant);
        return true;
    }

    public List<RestaurantModel> getTopRatedActiveRestaurants(int limit) {
        // Use Pageable to request top 'limit' restaurants ordered by overallRating desc
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, Math.max(1, limit), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("overallRating")));
        return restaurantRepository.findByStatus(Status.ACTIVE, pageable).stream().toList();
    }

    //**
    // * Retrieves the highest-rated active restaurants.
    // *
     //* Restaurants are sorted in descending order by overallRating.
    // * The number of results is limited by the provided parameter.
    // *
    // * Example:
    // * getTopRatedActiveRestaurants(5)
   //  * returns the top 5 active restaurants.

    // * return List of top-rated active restaurants.

    public List<RestaurantModel> getAllRestaurants(){

        return restaurantRepository.findAll();
    }

    /**
     * Retrieves a restaurant by its unique ID.

     * If no restaurant exists with the given ID,
     * NoSuchElementException is thrown.
     *
    * param id Restaurant ID.
    * return RestaurantModel with the given ID.
     */

    public RestaurantModel getRestaurantById(Long id){

        return restaurantRepository.findById(id)
                .orElseThrow();
    }

    /**
     * Saves a restaurant entity to the database.

     * This method can be used to create a new restaurant
     * or update an existing one.
     *
    * param restaurant Restaurant entity to save.
    * return The saved RestaurantModel.
     */

    public RestaurantModel saveRestaurant(RestaurantModel restaurant){

        return restaurantRepository.save(restaurant);
    }
}