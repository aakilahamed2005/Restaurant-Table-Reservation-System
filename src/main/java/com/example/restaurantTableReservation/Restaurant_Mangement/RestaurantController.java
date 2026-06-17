package com.example.restaurantTableReservation.Restaurant_Mangement;

import com.example.restaurantTableReservation.Feedback_Management.FeedbackModel;
import com.example.restaurantTableReservation.Feedback_Management.FeedbackRepository;
import com.example.restaurantTableReservation.Feedback_Management.FeedbackService;
import com.example.restaurantTableReservation.Restaurant_Mangement.dto.RestaurantFilterForm;
import com.example.restaurantTableReservation.Restaurant_Mangement.dto.RestaurantRegisterDto;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantScheduleModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantScheduleRepository;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantRepository;
import com.example.restaurantTableReservation.User_Management.Role;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.repository.UserRepository;
import com.example.restaurantTableReservation.User_Management.dto.RestaurantSupportContactRequestDto;
import com.example.restaurantTableReservation.User_Management.service.SupportContactMessageService;
import com.example.restaurantTableReservation.constant.ConstantData;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
public class RestaurantController {

    @Autowired
    RestaurantService restaurantService;
    // Service that handles restaurant-related operations (CRUD, listings, filters)

    @Autowired
    private FeedbackService feedbackService;
    // Service for managing feedback and review retrieval

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private RestaurantScheduleRepository restaurantScheduleRepository;

    @Autowired
    private RestaurantImageService restaurantImageService;
    // Helper service to fetch/store restaurant images and primary image paths

    @Autowired
    private UserRepository userRepository;

    // Repository for restaurant entities (used for stats and lookups)


    @Autowired
    private RestaurantRepository restaurantRepository;

    // Service to handle support contact messages from owners

    @Autowired
    private SupportContactMessageService supportContactMessageService;

    // Controller methods below handle public pages, owner flows and ajax endpoints

    /**
     * Displays the home page with featured restaurants and optional logged-in user details.
     */
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        // Retrieve logged in user from session, may be null for guests

        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        }

        // Decide whether to display guest links (no user/admin in session)
        boolean showGuestLinks = session.getAttribute("loggedInUser") == null
            && session.getAttribute("loggedAdmin") == null;
        model.addAttribute("showGuestLinks", showGuestLinks);

        // Fetch top 3 active restaurants to feature on the homepage
        List<RestaurantModel> topRestaurants = restaurantService.getTopRatedActiveRestaurants(3);
        model.addAttribute("restaurants", topRestaurants);
        model.addAttribute("primaryImagePaths", restaurantImageService.getPrimaryImagePaths(topRestaurants));

        // Calculate statistics from database
        // Total number of restaurants with ACTIVE status
        long totalActiveRestaurants = restaurantRepository.findAll().stream()
            .filter(r -> r.getStatus() == Status.ACTIVE)
            .count();

        long totalUsers = userRepository.count();

        // Calculate average rating across all active restaurants
        // Collect active restaurants first then compute average to avoid extra DB hits
        List<RestaurantModel> allActiveRestaurants = restaurantRepository.findAll().stream()
            .filter(r -> r.getStatus() == Status.ACTIVE)
            .toList();

        double averageRating = 0.0;
        if (!allActiveRestaurants.isEmpty()) {
            double totalRating = allActiveRestaurants.stream()
                    .mapToDouble(RestaurantModel::getOverallRating)
                    .sum();
            averageRating = Math.round((totalRating / allActiveRestaurants.size()) * 10.0) / 10.0;
        }

        model.addAttribute("totalRestaurants", totalActiveRestaurants);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("averageRating", averageRating);

        return "RestaurantManagement/index";
    }

    /**
     * Shows the public restaurant listing page with only active restaurants.
     */
    @GetMapping("/restaurant-listing")
    public String restaurantListing(Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        // Retrieve only active restaurants for the public listing
        List<RestaurantModel> restaurants = restaurantService.getAllRestaurants()
            .stream()
            .filter(r -> r.getStatus() == Status.ACTIVE)
            .toList();

        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        }

        model.addAttribute("filter_form", new RestaurantFilterForm());
        model.addAttribute("cuisines_list", ConstantData.cuisines);
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("primaryImagePaths", restaurantImageService.getPrimaryImagePaths(restaurants));
        model.addAttribute("restaurantReviewCounts", getRestaurantReviewCounts(restaurants));
        return "RestaurantManagement/restaurant-listing";
    }

    /**
     * Applies the restaurant filters submitted from the listing page.
     */
    @PostMapping("/restaurant-listing")
    public String applyFilter(@ModelAttribute("filter_form") RestaurantFilterForm filterForm, Model model, HttpSession session) {
        // Use service to apply user-provided filters and get matching restaurants
        List<RestaurantModel> results = restaurantService.filter(filterForm);
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        }

        model.addAttribute("filter_form", filterForm);
        model.addAttribute("cuisines_list", ConstantData.cuisines);
        model.addAttribute("restaurants", results);
        model.addAttribute("primaryImagePaths", restaurantImageService.getPrimaryImagePaths(results));
        model.addAttribute("restaurantReviewCounts", getRestaurantReviewCounts(results));

        return "RestaurantManagement/restaurant-listing";
    }

    private Map<Long, Integer> getRestaurantReviewCounts(List<RestaurantModel> restaurants) {
        Map<Long, Integer> reviewCounts = new HashMap<>();
        for (RestaurantModel restaurant : restaurants) {
            reviewCounts.put(restaurant.getId(), feedbackRepository.countByRestaurant(restaurant));
        }
        return reviewCounts;
    }

    /**
     * Opens the public restaurant detail page with reviews and schedule information.
     */
    @GetMapping("/restaurant-view-page/{id}")
    public String restaurantPage(@PathVariable Long id, HttpSession session, Model model) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        boolean isAdmin = session.getAttribute("loggedAdmin") != null;

        RestaurantModel restaurant = restaurantService.getRestaurantForView(id, user, isAdmin);

        if (restaurant != null) {
            List<FeedbackModel> restaurantFeedbacks = feedbackService.getAllFeedbackForRestaurant(restaurant);
            model.addAttribute("reviews", restaurantFeedbacks);

            List<RestaurantScheduleModel> schedules = restaurant.getSchedules();
            model.addAttribute("schedules", schedules);
        }

        if (restaurant == null) {
            return "RestaurantManagement/index";
        }

        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        }

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("restaurantImages", restaurantImageService.getImagesForRestaurant(restaurant));

        return "RestaurantManagement/restaurant-page";
    }

    /**
     * Shows the restaurant removal confirmation page for the logged-in owner.
     */
    @GetMapping("/restaurant-remove/{id}")
    public String restaurantRemove(@PathVariable Long id,
                                   HttpSession session,
                                   Model model){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        RestaurantModel restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null || !restaurant.getOwner().getId().equals(user.getId())) {
            return "redirect:/owner-restaurants";
        }

        model.addAttribute("user", user);
        model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("restaurant", restaurant);
        if (!model.containsAttribute("deleteError")) {
            model.addAttribute("deleteError", false);
        }

        return "RestaurantManagement/restaurant-remove";
    }

    /**
     * Processes the restaurant deletion request for the logged-in owner.
     */
    @PostMapping("/restaurant-remove/{id}")
    public String restaurantRemovePost(@PathVariable Long id,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        boolean removed = restaurantService.removeRestaurant(id, user);
        if (removed) {
            return "redirect:/owner-restaurants";
        }

        redirectAttributes.addFlashAttribute("deleteError", true);
        return "redirect:/restaurant-remove/" + id;
    }

    /**
     * Shows the logged-in owner's restaurant dashboard and status counts.
     */
    @GetMapping("/owner-restaurants")
    public String ownerRestaurants(HttpSession session, Model model){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null){
            return "redirect:/sign-in?tab=partner";
        }

        List<RestaurantModel> restaurants = restaurantService.getAllRestaurantsOfOwner(user);

        int noOfApprovedRestaurants = 0;
        int noOfPendingRestaurants = 0;
        int noOfSuspendedRestaurants = 0;
        int noOfDeactivatedRestaurants = 0;

        for (RestaurantModel restaurant: restaurants){
            if (restaurant.getStatus() == Status.ACTIVE) {
                noOfApprovedRestaurants++;
            } else if (restaurant.getStatus() == Status.SUSPENDED) {
                noOfSuspendedRestaurants++;
            } else if (restaurant.getStatus() == Status.PENDING) {
                noOfPendingRestaurants++;
            } else if (restaurant.getStatus() == Status.DEACTIVATED) {
                noOfDeactivatedRestaurants++;
            }
        }

        model.addAttribute("restaurants", restaurants);
        model.addAttribute("primaryImagePaths", restaurantImageService.getPrimaryImagePaths(restaurants));
        model.addAttribute("noOfTotalRestaurants", restaurants.size());
        model.addAttribute("noOfApprovedRestaurants", noOfApprovedRestaurants);
        model.addAttribute("noOfPendingRestaurants", noOfPendingRestaurants);
        model.addAttribute("noOfSuspendedRestaurants", noOfSuspendedRestaurants);
        model.addAttribute("noOfDeactivatedRestaurants", noOfDeactivatedRestaurants);
        model.addAttribute("user", user);
        model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));

        return "RestaurantManagement/owner-restaurants";
    }

    /**
     * Displays the restaurant registration form for the logged-in owner.
     */
    @GetMapping("/restaurant-register")
    public String restaurantRegister(Model model, HttpSession session){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null){
            return "redirect:/sign-in?tab=partner";
        }

        model.addAttribute("user", user);
        model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("cuisines", ConstantData.cuisines);
        model.addAttribute("restaurantRegisterDto", new RestaurantRegisterDto());

        return "RestaurantManagement/restaurant-register";
    }
    /**
     * Saves a newly registered restaurant for the logged-in owner.
     */


     @PostMapping("/restaurant-register")
    public String restaurantRegisterPost(@Valid @ModelAttribute("restaurantRegisterDto") RestaurantRegisterDto restaurantRegisterDto,
                                         BindingResult bindingResult,
                                         @RequestParam(value = "images", required = false) MultipartFile[] images,
                                         HttpSession session,
                                         Model model){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            model.addAttribute("cuisines", ConstantData.cuisines);
            model.addAttribute("restaurantRegisterDto", restaurantRegisterDto);
            return "RestaurantManagement/restaurant-register";
        }

        try {
            List<MultipartFile> imageList = images != null ? Arrays.asList(images) : Collections.emptyList();
            restaurantService.register(restaurantRegisterDto, user, imageList);
            return "redirect:/owner-restaurants";
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            model.addAttribute("cuisines", ConstantData.cuisines);
            model.addAttribute("restaurantRegisterDto", restaurantRegisterDto);
            model.addAttribute("error", e.getMessage());
            return "RestaurantManagement/restaurant-register";
        }
    }

    /**
     * Loads the edit form with the restaurant's existing details and schedules.
     */
    @GetMapping("/restaurant-edit/{id}")
    public String restaurantEdit(@PathVariable Long id, Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/sign-in?tab=partner";

        RestaurantModel restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null || !restaurant.getOwner().getId().equals(user.getId())) {
            return "redirect:/owner-restaurants";
        }

        RestaurantRegisterDto dto = new RestaurantRegisterDto();
        dto.setName(restaurant.getRestaurantName());
        dto.setCuisine(restaurant.getCuisine());
        dto.setPriceRange(restaurant.getPriceRange());
        dto.setShortDescription(restaurant.getShortDescription());
        dto.setDescription(restaurant.getDescription());
        dto.setStreetAddress(restaurant.getStreetAddress());
        dto.setCity(restaurant.getCity());
        dto.setProvince(restaurant.getProvince());
        dto.setPhoneNumber(restaurant.getPhoneNumber());
        dto.setEmail(restaurant.getEmail());
        dto.setWebsite(restaurant.getWebsite());
        dto.setSeatingCapacity(restaurant.getTotalSeats());
        dto.setMaxPartySize(restaurant.getMaxPartySize());
        dto.setHasParking(restaurant.isHasParking());
        dto.setHasWifi(restaurant.isHasWifi());
        dto.setHalalCertified(restaurant.isHalalCertified());

        List<RestaurantScheduleModel> schedules = restaurantScheduleRepository.findByRestaurant(restaurant);
        if (schedules != null) {
            for (RestaurantScheduleModel s : schedules) {
                switch (s.getDay()) {
                    case MONDAY:
                        dto.setMondayOpen(!s.isClosed());
                        dto.setMondayOpeningTime(s.getOpenTime());
                        dto.setMondayClosingTime(s.getCloseTime());
                        break;
                    case TUESDAY:
                        dto.setTuesdayOpen(!s.isClosed());
                        dto.setTuesdayOpeningTime(s.getOpenTime());
                        dto.setTuesdayClosingTime(s.getCloseTime());
                        break;
                    case WEDNESDAY:
                        dto.setWednesdayOpen(!s.isClosed());
                        dto.setWednesdayOpeningTime(s.getOpenTime());
                        dto.setWednesdayClosingTime(s.getCloseTime());
                        break;
                    case THURSDAY:
                        dto.setThursdayOpen(!s.isClosed());
                        dto.setThursdayOpeningTime(s.getOpenTime());
                        dto.setThursdayClosingTime(s.getCloseTime());
                        break;
                    case FRIDAY:
                        dto.setFridayOpen(!s.isClosed());
                        dto.setFridayOpeningTime(s.getOpenTime());
                        dto.setFridayClosingTime(s.getCloseTime());
                        break;
                    case SATURDAY:
                        dto.setSaturdayOpen(!s.isClosed());
                        dto.setSaturdayOpeningTime(s.getOpenTime());
                        dto.setSaturdayClosingTime(s.getCloseTime());
                        break;
                    case SUNDAY:
                        dto.setSundayOpen(!s.isClosed());
                        dto.setSundayOpeningTime(s.getOpenTime());
                        dto.setSundayClosingTime(s.getCloseTime());
                        break;
                }
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("cuisines", ConstantData.cuisines);
        model.addAttribute("restaurantRegisterDto", dto);
        model.addAttribute("restaurantId", id);
        model.addAttribute("weekDays", List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
        model.addAttribute("existingImages", restaurantImageService.getImagesForRestaurant(restaurant));

        return "RestaurantManagement/restaurant-edit";
    }

    /**
     * Updates the selected restaurant using the submitted edit form data.
     */


    @PostMapping("/restaurant-edit/{restaurantId}/images/{imageId}/delete")
    public String deleteRestaurantImage(@PathVariable Long restaurantId,
                                        @PathVariable Long imageId,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        RestaurantModel restaurant = restaurantService.getRestaurantById(restaurantId);
        if (restaurant == null || !restaurant.getOwner().getId().equals(user.getId())) {
            return "redirect:/owner-restaurants";
        }

        restaurantImageService.deleteImages(restaurant, List.of(imageId));
        redirectAttributes.addFlashAttribute("message", "Photo removed.");
        return "redirect:/restaurant-edit/" + restaurantId;
    }

    @PostMapping("/restaurant-edit/{id}")
    public String restaurantEditPost(@PathVariable Long id,
                                     @Valid @ModelAttribute("restaurantRegisterDto") RestaurantRegisterDto dto,
                                     BindingResult bindingResult,
                                     @RequestParam(value = "images", required = false) MultipartFile[] images,
                                     @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/sign-in?tab=partner";

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            RestaurantModel restaurant = restaurantService.getRestaurantById(id);
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            model.addAttribute("cuisines", ConstantData.cuisines);
            model.addAttribute("restaurantRegisterDto", dto);
            model.addAttribute("restaurantId", id);
            model.addAttribute("weekDays", List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
            model.addAttribute("existingImages", restaurantImageService.getImagesForRestaurant(restaurant));
            return "RestaurantManagement/restaurant-edit";
        }

        try {
            List<MultipartFile> imageList = images != null ? Arrays.asList(images) : Collections.emptyList();
            List<Long> idsToDelete = deleteImageIds != null ? deleteImageIds : Collections.emptyList();
            restaurantService.update(dto, id, user, imageList, idsToDelete);
            return "redirect:/restaurant-view-page/" + id;
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/restaurant-edit/" + id;
        }
    }

    /**
     * Logged-in owners can submit a support message about a suspended listing (used from owner dashboard modal).
     */
    @ResponseBody
    @PostMapping(value = "/owner-restaurant-support-contact", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submitRestaurantSupportContact(@RequestBody RestaurantSupportContactRequestDto dto,
                                                                              HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null || user.getRole() != Role.OWNER) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "message", "Please sign in as a restaurant partner."));
        }
        if (dto.getRestaurantId() == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Restaurant is required."));
        }
        RestaurantModel restaurant = restaurantRepository.findById(dto.getRestaurantId()).orElse(null);
        if (restaurant == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Restaurant not found."));
        }
        if (restaurant.getOwner() == null || !restaurant.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "message", "You can only send messages about your own restaurants."));
        }
        if (restaurant.getStatus() != Status.SUSPENDED) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Support contact is only available for suspended listings."));
        }
        try {
            supportContactMessageService.saveRestaurantListingMessage(user, restaurant, dto);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
