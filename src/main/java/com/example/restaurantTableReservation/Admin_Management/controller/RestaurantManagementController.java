package com.example.restaurantTableReservation.Admin_Management.controller;

/*
 * RestaurantManagementController — admin moderation of owner restaurants.
 *
 * GET  /admin/restaurants              — list with cuisine/status/keyword filters; cuisines from ConstantData
 * GET  /admin/restaurants/{id}/details — JSON for view modal (schedules, images, stats)
 * POST approve/suspend/reactivate      — change Status (PENDING, ACTIVE, SUSPENDED, DEACTIVATED handling in UI)
 * Template: restaurant-management.html
 */

import com.example.restaurantTableReservation.Admin_Management.util.AdminFilterHelper;
import com.example.restaurantTableReservation.constant.ConstantData;
import com.example.restaurantTableReservation.Reservation_Management.ReservationRepository;
import com.example.restaurantTableReservation.Restaurant_Mangement.RestaurantService;
import com.example.restaurantTableReservation.Restaurant_Mangement.Status;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantScheduleModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantRepository;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantScheduleRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RestaurantManagementController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantScheduleRepository restaurantScheduleRepository;

    @GetMapping("/admin/restaurants")
    public String restaurantManagementPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tab,
            Model model,
            HttpSession session
    ) {

        List<RestaurantModel> allRestaurants = restaurantService.getAllRestaurants();
        List<RestaurantModel> restaurants = allRestaurants;

        if (AdminFilterHelper.hasFilterValue(keyword)) {
            String search = keyword.trim().toLowerCase();
            restaurants = restaurants.stream()
                    .filter(r ->
                            (r.getRestaurantName() != null && r.getRestaurantName().toLowerCase().contains(search))
                                    || (r.getCity() != null && r.getCity().toLowerCase().contains(search))
                    )
                    .toList();
        }

        if (AdminFilterHelper.hasFilterValue(cuisine)) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getCuisine() != null && r.getCuisine().equalsIgnoreCase(cuisine.trim()))
                    .toList();
        }

        if (AdminFilterHelper.hasFilterValue(status)) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getStatus().name().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        long totalRestaurants = allRestaurants.size();
        long activeRestaurants = allRestaurants.stream()
                .filter(r -> r.getStatus().name().equals("ACTIVE"))
                .count();
        long pendingRestaurants = allRestaurants.stream()
                .filter(r -> r.getStatus().name().equals("PENDING"))
                .count();
        long suspendedRestaurants = allRestaurants.stream()
                .filter(r -> r.getStatus().name().equals("SUSPENDED"))
                .count();

        List<RestaurantModel> pendingRestaurantList = allRestaurants.stream()
                .filter(r -> r.getStatus() == Status.PENDING)
                .toList();

        List<RestaurantModel> suspendedRestaurantList = allRestaurants.stream()
                .filter(r -> r.getStatus() == Status.SUSPENDED)
                .toList();

        model.addAttribute("cuisines_list", ConstantData.cuisines);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("cuisine", cuisine != null ? cuisine : "");
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("totalRestaurants", totalRestaurants);
        model.addAttribute("activeRestaurants", activeRestaurants);
        model.addAttribute("pendingRestaurants", pendingRestaurants);
        model.addAttribute("suspendedRestaurants", suspendedRestaurants);
        model.addAttribute("pendingRestaurantList", pendingRestaurantList);
        model.addAttribute("suspendedRestaurantList", suspendedRestaurantList);

        String activeTab = "tab-all";
        if ("pending".equalsIgnoreCase(tab)) {
            activeTab = "tab-pending";
        } else if ("suspended".equalsIgnoreCase(tab)) {
            activeTab = "tab-suspended";
        }
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("loggedAdmin", session.getAttribute("loggedAdmin"));

        return "AdminManagement/restaurant-management";
    }

    @GetMapping("/admin/restaurants/{id}/details")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getRestaurantDetails(@PathVariable Long id) {

        RestaurantModel restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, Object> details = new HashMap<>();

        details.put("id", restaurant.getId());
        details.put("name", restaurant.getRestaurantName());
        details.put("cuisine", restaurant.getCuisine() != null ? restaurant.getCuisine() : "—");
        details.put("city", restaurant.getCity() != null ? restaurant.getCity() : "—");
        details.put("address", restaurant.getStreetAddress() != null ? restaurant.getStreetAddress() : "—");
        details.put("email", restaurant.getEmail() != null ? restaurant.getEmail() : "—");
        details.put("phone", restaurant.getPhoneNumber() != null ? restaurant.getPhoneNumber() : "—");
        details.put("seats", restaurant.getTotalSeats());
        details.put("rating", restaurant.getOverallRating());
        details.put("status", restaurant.getStatus().name());
        details.put("description", restaurant.getShortDescription() != null ? restaurant.getShortDescription() : "");
        details.put("priceRange", restaurant.getPriceRange() != null ? restaurant.getPriceRange() : "—");
        details.put("reservationCount", reservationRepository.findByRestaurantId(id).size());

        if (restaurant.getOwner() != null) {
            details.put("ownerName",
                    restaurant.getOwner().getFirstName() + " " + restaurant.getOwner().getLastName());
            details.put("ownerEmail", restaurant.getOwner().getEmail());
        } else {
            details.put("ownerName", "—");
            details.put("ownerEmail", "—");
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        List<RestaurantScheduleModel> schedules = restaurantScheduleRepository.findByRestaurant(restaurant);
        List<Map<String, String>> weeklySchedule = buildWeeklySchedule(schedules, timeFormatter);
        details.put("weeklySchedule", weeklySchedule);

        String openingHours = "—";
        String closingHours = "—";
        List<Map<String, String>> openDays = weeklySchedule.stream()
                .filter(row -> !"Closed".equals(row.get("hours")))
                .toList();
        if (!openDays.isEmpty()) {
            String firstHours = openDays.get(0).get("hours");
            boolean allSame = openDays.stream().allMatch(row -> firstHours.equals(row.get("hours")));
            if (allSame && openDays.size() == 7) {
                String[] parts = firstHours.split(" - ", 2);
                openingHours = parts[0];
                closingHours = parts.length > 1 ? parts[1] : "—";
            } else if (allSame) {
                String[] parts = firstHours.split(" - ", 2);
                openingHours = parts[0];
                closingHours = parts.length > 1 ? parts[1] : "—";
            } else {
                openingHours = "See weekly hours";
                closingHours = "—";
            }
        }
        details.put("openingHours", openingHours);
        details.put("closingHours", closingHours);

        return ResponseEntity.ok(details);
    }

    private List<Map<String, String>> buildWeeklySchedule(
            List<RestaurantScheduleModel> schedules,
            DateTimeFormatter timeFormatter
    ) {
        List<Map<String, String>> weeklySchedule = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            Map<String, String> row = new HashMap<>();
            row.put("day", formatDayLabel(day));

            RestaurantScheduleModel schedule = null;
            if (schedules != null) {
                schedule = schedules.stream()
                        .filter(s -> s.getDay() == day)
                        .findFirst()
                        .orElse(null);
            }

            if (schedule == null || schedule.isClosed()) {
                row.put("hours", "Closed");
            } else {
                String open = schedule.getOpenTime() != null
                        ? schedule.getOpenTime().format(timeFormatter)
                        : "—";
                String close = schedule.getCloseTime() != null
                        ? schedule.getCloseTime().format(timeFormatter)
                        : "—";
                row.put("hours", open + " - " + close);
            }
            weeklySchedule.add(row);
        }
        return weeklySchedule;
    }

    private String formatDayLabel(DayOfWeek day) {
        String name = day.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    @PostMapping("/admin/restaurants/approve/{id}")
    public String approveRestaurant(@PathVariable Long id) {
        RestaurantModel restaurant = restaurantService.getRestaurantById(id);
        restaurant.setStatus(Status.ACTIVE);
        restaurantService.saveRestaurant(restaurant);
        return "redirect:/admin/restaurants";
    }

    @PostMapping("/admin/restaurants/{id}/approve")
    public String approveRestaurantAlias(@PathVariable Long id) {
        return approveRestaurant(id);
    }

    @PostMapping("/admin/restaurants/suspend/{id}")
    public String suspendRestaurant(@PathVariable Long id) {
        restaurantService.suspendRestaurant(id);
        return "redirect:/admin/restaurants";
    }

    @PostMapping("/admin/restaurants/{id}/suspend")
    public String suspendRestaurantAlias(@PathVariable Long id) {
        return suspendRestaurant(id);
    }

    @PostMapping("/admin/restaurants/{id}/reactivate")
    public String reactivateRestaurant(@PathVariable Long id) {

        RestaurantModel restaurant = restaurantRepository.findById(id).orElseThrow();
        restaurant.setStatus(Status.ACTIVE);
        restaurantRepository.save(restaurant);

        return "redirect:/admin/restaurants";
    }
}
