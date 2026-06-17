package com.example.restaurantTableReservation.Admin_Management.controller; // Package: admin HTTP controllers

import com.example.restaurantTableReservation.User_Management.repository.SupportContactMessageRepository; // Count unread support tickets
import jakarta.servlet.http.HttpSession; // Browser session (loggedAdmin lives here)
import org.springframework.ui.Model; // Model = data passed to Thymeleaf HTML
import org.springframework.web.bind.annotation.ControllerAdvice; // Runs before controller methods
import org.springframework.web.bind.annotation.ModelAttribute; // Adds attributes to Model automatically

/*
 * Before every admin page renders, adds unreadSupportMessageCount to the Model
 * so notification-bell.html can show the red badge number.
 */
@ControllerAdvice(basePackages = "com.example.restaurantTableReservation.Admin_Management.controller")
// ^ Only applies to classes in Admin_Management.controller package
public class AdminSupportUnreadModelAdvice {

    private final SupportContactMessageRepository supportContactMessageRepository; // DB access for support messages

    // Constructor injection: Spring passes the repository bean
    public AdminSupportUnreadModelAdvice(SupportContactMessageRepository supportContactMessageRepository) {
        this.supportContactMessageRepository = supportContactMessageRepository;
    }

    @ModelAttribute // Method runs before each handler; return value not used — we use model.addAttribute
    public void addUnreadSupportCount(HttpSession session, Model model) {
        if (session != null && session.getAttribute("loggedAdmin") != null) { // Only if admin is logged in
            // Count rows where admin has not opened the message yet
            model.addAttribute("unreadSupportMessageCount",
                    supportContactMessageRepository.countByAdminReadFalse());
        }
    }
}
