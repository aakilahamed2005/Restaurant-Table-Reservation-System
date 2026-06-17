package com.example.restaurantTableReservation.User_Management.controller;


import com.example.restaurantTableReservation.User_Management.Role;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.dto.PasswordChangeDto;
import com.example.restaurantTableReservation.User_Management.dto.UpdateDto;
import com.example.restaurantTableReservation.User_Management.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class UserController {

    @Autowired
    UserService userService;

    // View GET request
    @GetMapping("/customer-profile-view")
    public String customer_profile_view(Model model, HttpSession session){

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null){
            return "redirect:/";
        }

        //if this user is a restaurant owner then need to find out his how many restaurants he has

        model.addAttribute("userFullName",user.getFirstName() + " " + user.getLastName());
        model.addAttribute("userFirstName",user.getFirstName());
        model.addAttribute("userLastName",user.getLastName());
        model.addAttribute("userEmail",user.getEmail());
        model.addAttribute("userPhoneNumber",user.getPhoneNumber());
        model.addAttribute("firstLetter",user.getFirstName().charAt(0));


        model.addAttribute("totalBookings", userService.getTotalBookingsForCustomer(user.getId()));
        model.addAttribute("upcomingBookings", userService.getUpcomingBookingsForCustomer(user.getId()));
        model.addAttribute("avgReview", userService.getAverageReviewRatingForCustomer(user.getId()));

        String successMessage = (String) session.getAttribute("successMessage");
        model.addAttribute("successMessage", successMessage);
        session.removeAttribute("successMessage");

        return "UserManagement/customer-profile-view";
    }

    // View GET request
    @GetMapping("/owner-profile-view")
    public String owner_profile_view(Model model, HttpSession session){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null){
            return "redirect:/";
        }


        model.addAttribute("userFullName",user.getFirstName() + " " + user.getLastName());
        model.addAttribute("userFirstName",user.getFirstName());
        model.addAttribute("userLastName",user.getLastName());
        model.addAttribute("userEmail",user.getEmail());
        model.addAttribute("userPhoneNumber",user.getPhoneNumber());
        model.addAttribute("firstLetter",user.getFirstName().charAt(0));

        model.addAttribute("totalRestaurants", userService.getTotalRestaurantsForOwner(user.getId()));
        model.addAttribute("avgRating", userService.getAverageRatingForOwner(user.getId()));


        String successMessage = (String) session.getAttribute("successMessage");
        model.addAttribute("successMessage", successMessage);
        session.removeAttribute("successMessage");

        return "UserManagement/owner-profile-view";

    }


    //Edit GET request
    @GetMapping("/customer-profile-edit")
    public String profile_edit_customer(Model model, HttpSession session){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");


        if (user == null){
            return "redirect:/";
        }

        UpdateDto updateDto = new UpdateDto();

        updateDto.setFirstName(user.getFirstName());
        updateDto.setLastName(user.getLastName());
        updateDto.setEmail(user.getEmail());
        updateDto.setPhoneNumber(user.getPhoneNumber());


        model.addAttribute("updateDto", updateDto);
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        model.addAttribute("user", user);
        model.addAttribute("userFullName",user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter",user.getFirstName().charAt(0));


        return "UserManagement/customer-profile-edit";
    }

    //Edit GET request
    @GetMapping("/owner-profile-edit")
    public String profile_edit_owner(Model model, HttpSession session){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");


        if (user == null){
            return "redirect:/";
        }

        UpdateDto updateDto = new UpdateDto();

        updateDto.setFirstName(user.getFirstName());
        updateDto.setLastName(user.getLastName());
        updateDto.setEmail(user.getEmail());
        updateDto.setPhoneNumber(user.getPhoneNumber());


        model.addAttribute("updateDto", updateDto);
        model.addAttribute("passwordChangeDto", new PasswordChangeDto());
        model.addAttribute("user", user);
        model.addAttribute("userFullName",user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter",user.getFirstName().charAt(0));


        return "UserManagement/owner-profile-edit";



    }

    //EDIT POST
    @PostMapping("/customer-profile-edit")
    public String updateCustomerProfile(@Valid @ModelAttribute("updateDto") UpdateDto updateDto,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/";
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("updateDto", updateDto);
            model.addAttribute("passwordChangeDto", new PasswordChangeDto());
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            return "UserManagement/customer-profile-edit";
        }

        if (userService.isPhoneNumberUsedByAnotherUser(updateDto.getPhoneNumber(), user.getId())) {
            bindingResult.rejectValue("phoneNumber", "phoneNumber.exists", "Phone number already exists");
            model.addAttribute("updateDto", updateDto);
            model.addAttribute("passwordChangeDto", new PasswordChangeDto());
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            return "UserManagement/customer-profile-edit";
        }

        UserModel updatedUser = userService.update(updateDto, user);


        session.setAttribute("loggedInUser", updatedUser);
        redirectAttributes.addFlashAttribute("successMessage", "Saved successfully");

        return "redirect:/customer-profile-view";

    }

    //EDIT POST
    @PostMapping("/owner-profile-edit")
    public String updateOwnerProfile(@Valid @ModelAttribute("updateDto") UpdateDto updateDto,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/";
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("updateDto", updateDto);
            model.addAttribute("passwordChangeDto", new PasswordChangeDto());
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            return "UserManagement/owner-profile-edit";
        }

        if (userService.isPhoneNumberUsedByAnotherUser(updateDto.getPhoneNumber(), user.getId())) {
            bindingResult.rejectValue("phoneNumber", "phoneNumber.exists", "Phone number already exists");
            model.addAttribute("updateDto", updateDto);
            model.addAttribute("passwordChangeDto", new PasswordChangeDto());
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            return "UserManagement/owner-profile-edit";
        }

        UserModel updatedUser = userService.update(updateDto, user);


        session.setAttribute("loggedInUser", updatedUser);
        redirectAttributes.addFlashAttribute("successMessage", "Saved successfully");

        return "redirect:/owner-profile-view";

    }




    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordChangeDto") PasswordChangeDto passwordChangeDto,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            UpdateDto updateDto = new UpdateDto();
            updateDto.setFirstName(user.getFirstName());
            updateDto.setLastName(user.getLastName());
            updateDto.setEmail(user.getEmail());
            updateDto.setPhoneNumber(user.getPhoneNumber());

            model.addAttribute("updateDto", updateDto);
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));

            if (user.getRole().equals(Role.OWNER)) {
                return "UserManagement/owner-profile-edit";
            }
            return "UserManagement/customer-profile-edit";
        }

        try{
            userService.changePassword(user, passwordChangeDto);
            //remove the session if the password is reset
            session.invalidate();
            return "redirect:/";

        }catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("error",  e.getMessage());
            if (user.getRole().equals(Role.OWNER))
                return "redirect:/owner-profile-edit";
            else
                return  "redirect:/customer-profile-edit";


        }
    }

}
