package com.example.restaurantTableReservation.Admin_Management.controller;

/*
 * AdminManagementController — CRUD for platform admin accounts (not end-users).
 *
 * SUPER_ADMIN: list all admins, create new admins, edit any admin including role.
 * Standard ADMIN: may only open edit form for their own account; role field is not updated on save.
 *
 * Routes:
 *   GET  /admin/manage-admins     — admin-management.html with stats and table
 *   POST /admin/create-admin      — register (super admin only)
 *   GET  /admin/edit/{id}         — edit-admin.html
 *   POST /admin/update            — save name/email/role (role only if super admin)
 */

import com.example.restaurantTableReservation.Admin_Management.model.Admin;
import com.example.restaurantTableReservation.Admin_Management.service.AdminService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminManagementController {

    @Autowired
    private AdminService adminService;

    // List admins and role counts for dashboard cards
    @GetMapping("/admin/manage-admins")
    public String manageAdminsPage(
            Model model,
            HttpSession session
    ) {

        Admin loggedAdmin = (Admin) session.getAttribute("loggedAdmin");
        List<Admin> admins = adminService.getAllAdmins();
        long totalAdmins = admins.size();
        long superAdminCount = admins.stream()
                .filter(a -> "SUPER_ADMIN".equals(a.getRole()))
                .count();
        long standardAdminCount = admins.stream()
                .filter(a -> "ADMIN".equals(a.getRole()))
                .count();

        model.addAttribute("admins", admins);
        model.addAttribute("loggedAdmin", loggedAdmin);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("superAdminCount", superAdminCount);
        model.addAttribute("standardAdminCount", standardAdminCount);
        model.addAttribute("isSuperAdmin",
                loggedAdmin != null && "SUPER_ADMIN".equals(loggedAdmin.getRole()));

        return "AdminManagement/admin-management";
    }

    // Create admin — blocked unless session role is SUPER_ADMIN
    @PostMapping("/admin/create-admin")
    public String createAdmin(
            @RequestParam String adminName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            HttpSession session
    ) {

        Admin loggedAdmin = (Admin) session.getAttribute("loggedAdmin");
        if (loggedAdmin == null || !"SUPER_ADMIN".equals(loggedAdmin.getRole())) {
            return "redirect:/admin/manage-admins";
        }

        Admin admin = new Admin();
        admin.setAdminName(adminName);
        admin.setEmail(email);
        admin.setPassword(password);
        admin.setRole(role);

        adminService.registerAdmin(admin);

        return "redirect:/admin/manage-admins";
    }

    // Edit form — super admin any id; normal admin only own row
    @GetMapping("/admin/edit/{id}")
    public String editAdminPage(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {

        Admin loggedAdmin = (Admin) session.getAttribute("loggedAdmin");

        if (loggedAdmin == null) {
            return "redirect:/admin/login";
        }

        Admin admin = adminService.getAdminById(id);

        boolean isSuperAdmin = "SUPER_ADMIN".equals(loggedAdmin.getRole());
        boolean editingOwnAccount = loggedAdmin.getEmail().equals(admin.getEmail());

        if (!isSuperAdmin && !editingOwnAccount) {
            return "redirect:/admin/manage-admins";
        }

        model.addAttribute("admin", admin);
        model.addAttribute("loggedAdmin", loggedAdmin);
        model.addAttribute("isSuperAdmin", isSuperAdmin);

        return "AdminManagement/edit-admin";
    }

    // Persist changes; role updated only when editor is SUPER_ADMIN
    @PostMapping("/admin/update")
    public String updateAdmin(
            @RequestParam Long adminId,
            @RequestParam String adminName,
            @RequestParam String email,
            @RequestParam String role,
            HttpSession session
    ) {

        Admin loggedAdmin = (Admin) session.getAttribute("loggedAdmin");

        if (loggedAdmin == null) {
            return "redirect:/admin/login";
        }

        Admin admin = adminService.getAdminById(adminId);

        boolean isSuperAdmin = "SUPER_ADMIN".equals(loggedAdmin.getRole());
        boolean editingOwnAccount = loggedAdmin.getEmail().equals(admin.getEmail());

        if (!isSuperAdmin && !editingOwnAccount) {
            return "redirect:/admin/manage-admins";
        }

        admin.setAdminName(adminName);
        admin.setEmail(email);

        if (isSuperAdmin) {
            admin.setRole(role);
        }

        adminService.updateExistingAdmin(admin);

        return "redirect:/admin/manage-admins";
    }
}
