package com.example.restaurantTableReservation.User_Management.controller;


import com.example.restaurantTableReservation.User_Management.exception.AccountDeactivatedException;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.dto.DeleteDto;
import com.example.restaurantTableReservation.User_Management.dto.LoginDto;
import com.example.restaurantTableReservation.User_Management.dto.RegisterDto;
import com.example.restaurantTableReservation.User_Management.service.UserAuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserAuthController {
    @Autowired
    UserAuthService userAuthService;

    @GetMapping("/customer-login")
    public String customerLoginRedirect(@RequestParam(required = false) String tab) {
        if ("partner".equalsIgnoreCase(tab) || "owner".equalsIgnoreCase(tab)) {
            return "redirect:/sign-in?tab=partner";
        }
        return "redirect:/sign-in?tab=customer";
    }

    @GetMapping("/sign-in")
    public String signInPage(
            @RequestParam(required = false) String tab,
            Model model
    ) {
        model.addAttribute("loginDto", new LoginDto());
        model.addAttribute("activeTab", resolveTab(tab));
        return "UserManagement/sign-in";
    }

    @GetMapping("/owner-login")
    public String ownerLoginRedirect() {
        return "redirect:/sign-in?tab=partner";
    }

    @PostMapping("/sign-in")
    public String signInPost(
            @ModelAttribute("loginDto") LoginDto loginDto,
            @RequestParam(defaultValue = "customer") String loginType,
            Model model,
            HttpSession session
    ) {
        String tab = resolveTab(loginType);

        try {
            UserModel user;
            if ("partner".equals(tab)) {
                user = userAuthService.ownerSignIn(loginDto);
                session.setAttribute("loggedInUser", user);
                return "redirect:/owner-profile-view";
            }

            user = userAuthService.customerSignIn(loginDto);
            session.setAttribute("loggedInUser", user);
            return "redirect:/restaurant-listing";

        } catch (AccountDeactivatedException e) {
            model.addAttribute("loginDto", loginDto);
            model.addAttribute("activeTab", tab);
            model.addAttribute("accountDeactivated", true);
            return "UserManagement/sign-in";
        } catch (RuntimeException e) {
            model.addAttribute("loginDto", loginDto);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("activeTab", tab);
            return "UserManagement/sign-in";
        }
    }


    @PostMapping("/owner-login")
    public String ownerLoginPostLegacy(
            @ModelAttribute("loginDto") LoginDto loginDto,
            Model model,
            HttpSession session
    ) {
        return signInPost(loginDto, "partner", model, session);
    }




    @PostMapping("/customer-login")
    public String customerLoginPostLegacy(@ModelAttribute("loginDto") LoginDto loginDto,
                                          Model model, HttpSession session
    ) {
        return signInPost(loginDto, "customer", model, session);
    }






    /*Registration Controllers*/

    @GetMapping("/customer-register")
    public String customer_register(Model model){
        model.addAttribute("registerDto", new RegisterDto());
        return "UserManagement/customer-register";
    }


    @GetMapping("/owner-register")
    public String owner_register(Model model){
        model.addAttribute("registerDto", new RegisterDto());
        return "UserManagement/owner-register";
    }


    //Register POST request
    @PostMapping("/customer-register")
    public String customerRegisterForm(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes, Model model){
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerDto", registerDto);
            return "UserManagement/customer-register";
        }

        try{
            userAuthService.register(registerDto, "customer");
            redirectAttributes.addFlashAttribute("success_msg", "Successfully Registered");

            return "redirect:/sign-in?tab=customer";

        }catch (RuntimeException e){
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDto", registerDto);

            return "UserManagement/customer-register";

        }

    }

    //Register POST request
    @PostMapping("/owner-register")
    public String ownerRegisterForm(@Valid @ModelAttribute("registerDto") RegisterDto registerDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes, Model model){
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerDto", registerDto);
            return "UserManagement/owner-register";
        }

        try{
            userAuthService.register(registerDto, "owner");
            redirectAttributes.addFlashAttribute("success_msg", "Successfully Registered");

            return "redirect:/sign-in?tab=partner";

        }catch (RuntimeException e){
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDto", registerDto);


            return "UserManagement/owner-register";
        }

    }


    @GetMapping("/customer-delete-account")
    public String customer_profile_delete(Model model, HttpSession session){

        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null){
            return "redirect:/";
        }

        model.addAttribute("user", user);
        model.addAttribute("userFullName",user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter",user.getFirstName().charAt(0));
        model.addAttribute("deleteForm",  new DeleteDto());


        return "UserManagement/customer-profile-delete";
    }

    @PostMapping("/customer-delete-account")
    public String customerProfileDeleteForm(@ModelAttribute("deleteForm") DeleteDto deleteForm, HttpSession session, Model model){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null){
            return "redirect:/";
        }
        try{
            userAuthService.deleteUser(user, deleteForm);
            return "redirect:/user-logout";

        }catch (RuntimeException e){
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("userFullName",user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter",user.getFirstName().charAt(0));
            model.addAttribute("deleteForm",  new DeleteDto());
            return "UserManagement/customer-profile-delete";
        }

    }

    @GetMapping("/owner-profile-delete")
    public String ownerDeleteAccount(Model model, HttpSession session){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null){
            return "redirect:/";
        }

        model.addAttribute("user", user);
        model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("deleteForm", new DeleteDto());

        return "UserManagement/owner-profile-delete";
    }

    @PostMapping("/owner-profile-delete")
    public String ownerDeleteAccountPost(@ModelAttribute("deleteForm") DeleteDto deleteForm, HttpSession session, Model model){
        UserModel user = (UserModel) session.getAttribute("loggedInUser");
        if (user == null){
            return "redirect:/";
        }
        try{
            userAuthService.deactivateOwnerAccount(user, deleteForm);
            return "redirect:/user-logout";

        }catch (RuntimeException e){
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            model.addAttribute("deleteForm", new DeleteDto());
            return "UserManagement/owner-profile-delete";
        }
    }

    //user logout
    @GetMapping("/user-logout")
    public String user_logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }


    //Select which text is passed in tab request and return whether customer or partner
    private String resolveTab(String tab) {
        if (tab == null) {
            return "customer";
        }
        String normalized = tab.trim().toLowerCase();
        if ("partner".equals(normalized) || "owner".equals(normalized)) {
            return "partner";
        }
        return "customer";
    }



}
