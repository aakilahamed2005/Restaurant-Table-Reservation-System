package com.example.restaurantTableReservation.Admin_Management.controller;

/*
 * UserManagementController — admin view of customers and owners.
 *
 * GET /admin/users — loads all users, optional filters: keyword (name/email/phone),
 *   role (CUSTOMER/OWNER), status (ACTIVE/DEACTIVATED/etc.). Renders user-management.html.
 * POST /admin/deactivate-user/{id} — delegates to UserAuthService (may suspend owner restaurants).
 * POST /admin/activate-user/{id} — restores user and suspended restaurants for owners.
 */

import com.example.restaurantTableReservation.Admin_Management.util.AdminFilterHelper;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.repository.UserRepository;
import com.example.restaurantTableReservation.User_Management.service.UserAuthService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthService userAuthService;

    @GetMapping("/admin/users")
    public String usersPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session
    ) {

        List<UserModel> users = userRepository.findAll();

        if (AdminFilterHelper.hasFilterValue(keyword)) {
            String search = keyword.trim().toLowerCase();
            users = users.stream()
                    .filter(user -> matchesUserSearch(user, search))
                    .toList();
        }

        if (AdminFilterHelper.hasFilterValue(role)) {
            users = users.stream()
                    .filter(user -> user.getRole().toString().equalsIgnoreCase(role.trim()))
                    .toList();
        }

        if (AdminFilterHelper.hasFilterValue(status)) {
            users = users.stream()
                    .filter(user -> user.getStatus().toString().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("role", role != null ? role : "");
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("loggedAdmin", session.getAttribute("loggedAdmin"));

        return "AdminManagement/user-management";
    }

    // Client-side filter: name, email, or phone contains search string
    private boolean matchesUserSearch(UserModel user, String search) {
        String firstName = user.getFirstName() != null ? user.getFirstName().toLowerCase() : "";
        String lastName = user.getLastName() != null ? user.getLastName().toLowerCase() : "";
        String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
        String phone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";
        String fullName = (firstName + " " + lastName).trim();

        return fullName.contains(search)
                || firstName.contains(search)
                || lastName.contains(search)
                || email.contains(search)
                || phone.contains(search);
    }

    @PostMapping("/admin/deactivate-user/{id}")
    public String deactivateUser(@PathVariable Long id) {

        UserModel user = userRepository.findById(id).orElse(null);

        if (user != null) {
            userAuthService.deactivateUserByAdmin(user);
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/activate-user/{id}")
    public String activateUser(@PathVariable Long id) {

        UserModel user = userRepository.findById(id).orElse(null);

        if (user != null) {
            userAuthService.activateUserByAdmin(user);
        }

        return "redirect:/admin/users";
    }
}
