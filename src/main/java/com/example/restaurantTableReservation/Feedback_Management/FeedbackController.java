package com.example.restaurantTableReservation.Feedback_Management;


import com.example.restaurantTableReservation.Restaurant_Mangement.RestaurantService;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class FeedbackController {

    @Autowired
    RestaurantService restaurantService;
    @Autowired
    private FeedbackService feedbackService;

    // Load the selected restaurant and prepare the feedback form for the logged-in customer
    @GetMapping("/restaurant-feedback/{id}")
    public String displayFeedBackForm(@PathVariable Long id, Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        RestaurantModel restaurant = restaurantService.getRestaurantById(id);

        if (user == null || restaurant == null) {
            return "redirect:/";
        }

        model.addAttribute("restaurant", restaurant);

        FeedbackDto form = new FeedbackDto();
        form.setRestaurantId(restaurant.getId());
        form.setCustomerId(user.getId());


        model.addAttribute("user", user);
        model.addAttribute("customerFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("feedback_form", form);

        return "FeedbackManagement/customer-feedback";

    }

    // Save the customer feedback and display the thank-you confirmation page
    @PostMapping("/restaurant-feedback")
    public String feedbackFormPost(@ModelAttribute("feedback_form") FeedbackDto feedback_form, HttpSession session, Model model) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        FeedbackModel feedback = feedbackService.createFeedback(feedback_form);

        model.addAttribute("feedback", feedback);
        model.addAttribute("customerFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("user", user);

        return "FeedbackManagement/feedback-thankyou";
    }

    // Calculate the total reviews, average rating, and number of five-star reviews for the customer
    @GetMapping("/customer-my-reviews")
    public String customerMyReviews(HttpSession session, Model model) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/sign-in";
        }

        List<FeedbackModel> feedbacks = feedbackService.getFeedbackByCustomerId(user.getId());
        int totalReviews = feedbacks == null ? 0 : feedbacks.size();
        int fiveStarCount = 0;
        double avgRating = 0.0;

        if (feedbacks != null && !feedbacks.isEmpty()) {
            int sum = 0;
            for (FeedbackModel feedback : feedbacks) {
                sum += feedback.getOverall_rating();
                if (feedback.getOverall_rating() == 5) {
                    fiveStarCount++;
                }
            }
            avgRating = Math.round((sum / (double) feedbacks.size()) * 10.0) / 10.0;
        }

        model.addAttribute("user", user);
        model.addAttribute("customerFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("fiveStarCount", fiveStarCount);
        model.addAttribute("avgRating", avgRating);

        return "FeedbackManagement/customer-my-reviews";
    }

    // Verify that the restaurant owner is logged in before allowing access to feedback details
    @GetMapping("/owner-restaurant-feedback/{id}")
    public String ownerRestaurantFeedback(@PathVariable Long id, HttpSession session, Model model) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        RestaurantModel restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null || restaurant.getOwner() == null || !restaurant.getOwner().getId().equals(user.getId())) {
            return "redirect:/owner-restaurants";
        }

        List<FeedbackModel> feedbacks = feedbackService.getFeedbacksForRestaurant(id);
        int totalFeedbacks = feedbacks == null ? 0 : feedbacks.size();
        int fiveStarCount = 0;
        double avgRating = 0.0;

        if (feedbacks != null && !feedbacks.isEmpty()) {
            int sum = 0;
            for (FeedbackModel feedback : feedbacks) {
                sum += feedback.getOverall_rating();
                if (feedback.getOverall_rating() == 5) {
                    fiveStarCount++;
                }
            }
            avgRating = Math.round((sum / (double) feedbacks.size()) * 10.0) / 10.0;
        }

        model.addAttribute("user", user);
        model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("totalFeedbacks", totalFeedbacks);
        model.addAttribute("fiveStarCount", fiveStarCount);
        model.addAttribute("avgRating", avgRating);

        return "FeedbackManagement/owner-restaurant-feedbacks";
    }


    // Update the customer's review details and redirect back to the review history page
    @PostMapping("/customer-my-reviews/edit")
    public String editCustomerReview(@RequestParam Long id,
                                     @RequestParam int overallRating,
                                     @RequestParam String review,
                                     HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/sign-in";
        }

        feedbackService.updateFeedback(id, overallRating, review);
        return "redirect:/customer-my-reviews";
    }


    // Delete the selected customer review and redirect to the customer's review list
    @PostMapping("/customer-my-reviews/delete/{id}")
    public String deleteCustomerReview(@PathVariable Long id, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/sign-in";
        }

        feedbackService.deleteFeedback(id, user);
        return "redirect:/customer-my-reviews";
    }


    // Verify that the user is logged in before allowing feedback deletion
    @PostMapping("/delete-feedback/{id}")
    public String deleteFeedback(@PathVariable Long id,
                                 HttpSession session) {

        UserModel user =
                (UserModel) session.getAttribute("loggedInUser");

        // Check login
        if (user == null) {
            return "redirect:/login";
        }

        // Delete feedback
        feedbackService.deleteFeedback(id, user);

        // Redirect after delete
        return "redirect:/";
    }


}