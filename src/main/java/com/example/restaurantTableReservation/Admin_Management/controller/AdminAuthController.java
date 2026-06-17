package com.example.restaurantTableReservation.Admin_Management.controller;

/*
 * AdminAuthController — authentication entry and dashboard shell.
 *
 * Responsibilities:
 * - Render admin login page and process POST login (email + password).
 * - On success, store Admin in HTTP session as "loggedAdmin" and redirect to dashboard.
 * - Invalidate session on logout and prevent browser back-cache of protected pages.
 * - Load dashboard KPIs (users, restaurants, reservations, feedback, weekly chart data).
 *
 * Templates: AdminManagement/admin-login, admin-dashboard.
 * Depends on: AdminService (credential check and aggregate counts).
 */

import com.example.restaurantTableReservation.Admin_Management.model.Admin;
import com.example.restaurantTableReservation.Admin_Management.service.AdminService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminAuthController {

    @Autowired
    private AdminService adminService;

    // GET — show login form (no session required)
    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "AdminManagement/admin-login";
    }

    // POST — validate credentials; set session or return error on same page
    @PostMapping("/admin/login")
    public String loginAdmin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {

        Admin admin = adminService.loginAdmin(email, password);

        if (admin != null) {
            session.setAttribute("loggedAdmin", admin);
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("error", "Invalid Email or Password");

        return "AdminManagement/admin-login";
    }

    // GET — destroy session and redirect to public home
    @GetMapping("/admin/logout")
    public String adminLogout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        return "redirect:/";
    }

    // GET — main admin home with statistics and recent activity feed
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {

        int[] weeklyCounts = adminService.getWeeklyReservationCounts();

        model.addAttribute("totalUsers", adminService.getTotalUsers());
        model.addAttribute("totalRestaurants", adminService.getTotalRestaurants());
        model.addAttribute("todayReservations", adminService.getTodayReservationsCount());
        model.addAttribute("totalReservations", adminService.getTotalReservations());
        model.addAttribute("pendingRestaurants", adminService.getPendingRestaurantsCount());
        model.addAttribute("activeRestaurants", adminService.getActiveRestaurantsCount());
        model.addAttribute("totalAdmins", adminService.getTotalAdmins());
        model.addAttribute("totalFeedback", adminService.getTotalFeedbackCount());
        model.addAttribute("pendingRestaurantList", adminService.getPendingRestaurantsForDashboard());
        model.addAttribute("weeklyCounts", weeklyCounts);
        model.addAttribute("weeklyBarHeights", adminService.getWeeklyBarHeights(weeklyCounts));
        model.addAttribute("weekDays", adminService.getWeekDayLabels());
        model.addAttribute("recentActivities", adminService.getRecentActivities());
        model.addAttribute("loggedAdmin", session.getAttribute("loggedAdmin"));

        return "AdminManagement/admin-dashboard";
    }
}
