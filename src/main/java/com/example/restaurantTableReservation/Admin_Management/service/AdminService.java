package com.example.restaurantTableReservation.Admin_Management.service;

/*
 * AdminService — business logic for admin accounts and dashboard aggregates.
 *
 * Auth: loginAdmin (BCrypt), registerAdmin (hash password), updateExistingAdmin.
 * Dashboard: counts for users, restaurants, reservations, feedback; weekly chart; recent activity list.
 * Admin CRUD: getAllAdmins, getAdminById, updateAdmin (password change path if used).
 */

import com.example.restaurantTableReservation.Admin_Management.dto.DashboardActivityItem;
import com.example.restaurantTableReservation.Admin_Management.model.Admin;
import com.example.restaurantTableReservation.Admin_Management.repository.AdminRepository;
import com.example.restaurantTableReservation.Feedback_Management.FeedbackModel;
import com.example.restaurantTableReservation.Feedback_Management.FeedbackRepository;
import com.example.restaurantTableReservation.Reservation_Management.ReservationModel;
import com.example.restaurantTableReservation.Reservation_Management.ReservationRepository;
import com.example.restaurantTableReservation.Restaurant_Mangement.Status;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantRepository;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    // Authenticate by email (case-insensitive) and BCrypt password hash
    public Admin loginAdmin(String email, String password) {

        Admin admin = adminRepository.findByEmailIgnoreCase(
                email != null ? email.trim() : ""
        );

        if (admin != null && BCrypt.checkpw(password, admin.getPassword())) {
            return admin;
        }

        return null;
    }

    public void registerAdmin(Admin admin) {

        String hashedPassword = BCrypt.hashpw(
                admin.getPassword(),
                BCrypt.gensalt()
        );

        admin.setPassword(hashedPassword);

        admin.setEmail(normalizeEmail(admin.getEmail()));

        if (adminRepository.findByEmailIgnoreCase(admin.getEmail()) != null) {
            throw new RuntimeException("Email already exists");
        }

        adminRepository.save(admin);
    }

    public void updateAdmin(Admin admin) {

        String hashedPassword = BCrypt.hashpw(
                admin.getPassword(),
                BCrypt.gensalt()
        );

        admin.setPassword(hashedPassword);

        adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin getAdminById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public void updateExistingAdmin(Admin admin) {
        adminRepository.save(admin);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getTotalRestaurants() {
        return restaurantRepository.count();
    }

    public long getTotalAdmins() {
        return adminRepository.count();
    }

    public long getTotalReservations() {
        return reservationRepository.count();
    }

    public long getTodayReservationsCount() {
        LocalDate today = LocalDate.now();
        return reservationRepository.findAll().stream()
                .filter(r -> r.getDate() != null && r.getDate().equals(today))
                .count();
    }

    public long getPendingRestaurantsCount() {
        return restaurantRepository.findAll().stream()
                .filter(r -> r.getStatus() == Status.PENDING)
                .count();
    }

    public long getActiveRestaurantsCount() {
        return restaurantRepository.findAll().stream()
                .filter(r -> r.getStatus() == Status.ACTIVE)
                .count();
    }

    public long getTotalFeedbackCount() {
        return feedbackRepository.count();
    }

    public List<RestaurantModel> getPendingRestaurantsForDashboard() {
        return restaurantRepository.findAll().stream()
                .filter(r -> r.getStatus() == Status.PENDING)
                .sorted(Comparator.comparing(RestaurantModel::getRestaurantName))
                .limit(5)
                .toList();
    }

    public int[] getWeeklyReservationCounts() {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        int[] counts = new int[7];

        for (ReservationModel reservation : reservationRepository.findAll()) {
            LocalDate date = reservation.getDate();
            if (date == null || date.isBefore(startOfWeek) || date.isAfter(endOfWeek)) {
                continue;
            }
            int index = date.getDayOfWeek().getValue() - 1;
            counts[index]++;
        }

        return counts;
    }

    public int getMaxWeeklyReservationCount(int[] weeklyCounts) {
        int max = 1;
        for (int count : weeklyCounts) {
            max = Math.max(max, count);
        }
        return max;
    }

    public List<Integer> getWeeklyBarHeights(int[] weeklyCounts) {
        int max = getMaxWeeklyReservationCount(weeklyCounts);
        List<Integer> heights = new ArrayList<>();
        for (int count : weeklyCounts) {
            heights.add(max > 0 ? Math.max(4, count * 100 / max) : 4);
        }
        return heights;
    }

    public List<String> getWeekDayLabels() {
        return List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
    }

    // Build mixed feed: new users, restaurants, reservations, feedback (newest first, capped)
    public List<DashboardActivityItem> getRecentActivities() {
        List<DashboardActivityItem> activities = new ArrayList<>();

        feedbackRepository.findAll().stream()
                .filter(f -> f.getCreatedAt() != null && f.getCustomer() != null && f.getRestaurant() != null)
                .sorted(Comparator.comparing(FeedbackModel::getCreatedAt).reversed())
                .limit(2)
                .forEach(f -> activities.add(new DashboardActivityItem(
                        "Customer <strong>" + f.getCustomer().getFirstName() + " " + f.getCustomer().getLastName()
                                + "</strong> reviewed <strong>" + f.getRestaurant().getRestaurantName() + "</strong>",
                        formatRelativeTime(f.getCreatedAt()),
                        "var(--terracotta)"
                )));

        restaurantRepository.findAll().stream()
                .filter(r -> r.getStatus() == Status.PENDING)
                .limit(2)
                .forEach(r -> activities.add(new DashboardActivityItem(
                        "Restaurant <strong>" + r.getRestaurantName() + "</strong> is pending approval",
                        "Awaiting review",
                        "var(--sage)"
                )));

        userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null)
                .sorted(Comparator.comparing(UserModel::getCreatedAt).reversed())
                .limit(2)
                .forEach(u -> activities.add(new DashboardActivityItem(
                        "New user <strong>" + u.getFirstName() + " " + u.getLastName() + "</strong> registered",
                        formatRelativeTime(u.getCreatedAt()),
                        "var(--gold)"
                )));

        reservationRepository.findAll().stream()
                .filter(r -> r.getCreatedAt() != null && r.getRestaurant() != null)
                .sorted(Comparator.comparing(ReservationModel::getCreatedAt).reversed())
                .limit(2)
                .forEach(r -> activities.add(new DashboardActivityItem(
                        "New reservation <strong>" + r.getReservationCode() + "</strong> at <strong>"
                                + r.getRestaurant().getRestaurantName() + "</strong>",
                        formatRelativeTime(r.getCreatedAt()),
                        "#6B7280"
                )));

        return activities.stream().limit(5).toList();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        }
        long days = duration.toDays();
        if (days == 1) {
            return "Yesterday";
        }
        if (days < 7) {
            return days + " days ago";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}
