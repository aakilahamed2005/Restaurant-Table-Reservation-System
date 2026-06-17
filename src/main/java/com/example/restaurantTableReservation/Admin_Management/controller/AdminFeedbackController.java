package com.example.restaurantTableReservation.Admin_Management.controller;

/*
 * AdminFeedbackController — browse and remove customer feedback.
 * GET /admin/feedback — keyword + star-rating filters; template feedback-management.html.
 * POST /admin/delete-feedback/{id} — hard delete via FeedbackService.deleteFeedbackByAdmin.
 */

import com.example.restaurantTableReservation.Admin_Management.util.AdminFilterHelper;
import com.example.restaurantTableReservation.Feedback_Management.FeedbackModel;
import com.example.restaurantTableReservation.Feedback_Management.FeedbackRepository;
import com.example.restaurantTableReservation.Feedback_Management.FeedbackService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminFeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/admin/feedback")
    public String feedbackManagementPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String rating,
            Model model,
            HttpSession session
    ) {

        List<FeedbackModel> feedbacks = feedbackRepository.findAll();

        if (AdminFilterHelper.hasFilterValue(keyword)) {
            String search = keyword.trim().toLowerCase();
            feedbacks = feedbacks.stream()
                    .filter(f -> matchesFeedbackSearch(f, search))
                    .toList();
        }

        if (AdminFilterHelper.hasFilterValue(rating)) {
            int ratingValue = Integer.parseInt(rating.trim());
            feedbacks = feedbacks.stream()
                    .filter(f -> f.getOverall_rating() == ratingValue)
                    .toList();
        }

        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("rating", rating != null ? rating : "");
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("loggedAdmin", session.getAttribute("loggedAdmin"));

        return "AdminManagement/feedback-management";
    }

    private boolean matchesFeedbackSearch(FeedbackModel feedback, String search) {
        if (feedback.getCustomer() != null) {
            String customerName = (feedback.getCustomer().getFirstName() + " "
                    + feedback.getCustomer().getLastName()).toLowerCase();
            if (customerName.contains(search)) {
                return true;
            }
            if (feedback.getCustomer().getEmail() != null
                    && feedback.getCustomer().getEmail().toLowerCase().contains(search)) {
                return true;
            }
        }
        return feedback.getRestaurant() != null
                && feedback.getRestaurant().getRestaurantName() != null
                && feedback.getRestaurant().getRestaurantName().toLowerCase().contains(search);
    }

    @PostMapping("/admin/delete-feedback/{id}")
    public String deleteFeedback(@PathVariable Long id) {

        feedbackService.deleteFeedbackByAdmin(id);

        return "redirect:/admin/feedback";
    }
}
