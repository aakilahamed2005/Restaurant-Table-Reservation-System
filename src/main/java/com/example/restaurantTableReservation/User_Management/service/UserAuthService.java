package com.example.restaurantTableReservation.User_Management.service;

import com.example.restaurantTableReservation.Reservation_Management.ReservationService;
import com.example.restaurantTableReservation.User_Management.Role;
import com.example.restaurantTableReservation.User_Management.Status;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.repository.UserRepository;
import com.example.restaurantTableReservation.User_Management.exception.AccountDeactivatedException;
import com.example.restaurantTableReservation.User_Management.dto.DeleteDto;
import com.example.restaurantTableReservation.User_Management.dto.LoginDto;
import com.example.restaurantTableReservation.User_Management.dto.RegisterDto;
import com.example.restaurantTableReservation.Restaurant_Mangement.RestaurantService;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

@Service
public class UserAuthService {

    private static final String OWNER_DEACTIVATION_CANCEL_MSG =
            "This restaurant is temporarily unavailable because the owner deactivated their account, so your reservation was automatically cancelled. We’re sorry for the inconvenience and hope you’ll book another restaurant soon.";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private RestaurantService restaurantService;

    public UserModel register(RegisterDto registerDto, String role) {
        String normalizedEmail = normalizeEmail(registerDto.getEmail());
        registerDto.setEmail(normalizedEmail);

        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email Already Exists");
        }

        if (userRepository.findByPhoneNumber(registerDto.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("Phone Number Already Exists");
        }

        String hashedPassword = BCrypt.hashpw(registerDto.getPassword(), BCrypt.gensalt());
        registerDto.setPassword(hashedPassword);

        UserModel userModel = new UserModel();
        userModel.setFirstName(registerDto.getFirstName());
        userModel.setLastName(registerDto.getLastName());
        userModel.setEmail(registerDto.getEmail());
        userModel.setPassword(hashedPassword);
        userModel.setPhoneNumber(registerDto.getPhoneNumber());

        userModel.setStatus(Status.ACTIVE);

        //Here also no admin register all the admin register is done in admin management
        if (role.equals("customer"))
            userModel.setRole(Role.CUSTOMER);
        else if (role.equals("owner"))
            userModel.setRole(Role.OWNER);
        else
            throw new RuntimeException("Server Error (Error: 1502)");



        return userRepository.save(userModel);
    }

    // Customer Login
    public UserModel customerSignIn(LoginDto loginDto) {

        UserModel user = userRepository.findByEmailIgnoreCase(normalizeEmail(loginDto.getEmail()))
                .orElseThrow(() -> new RuntimeException("Email not found"));

        // jBCrypt password check
        if (!BCrypt.checkpw(loginDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.getStatus().equals(Status.DEACTIVATED)) {
            throw new AccountDeactivatedException();
        }

        if (user.getRole().equals(Role.CUSTOMER))

            return user; // login successful
        else
            throw new RuntimeException("You are using a Owner Email");
    }

    // Owner Login
    public UserModel ownerSignIn(LoginDto loginDto) {

        UserModel user = userRepository.findByEmailIgnoreCase(normalizeEmail(loginDto.getEmail()))
                .orElseThrow(() -> new RuntimeException("Email not found"));

        // jBCrypt password check
        if (!BCrypt.checkpw(loginDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (user.getStatus().equals(Status.DEACTIVATED)) {
            throw new AccountDeactivatedException();
        }

        if (user.getRole().equals(Role.OWNER))
            return user; // login successful
        else
            throw new RuntimeException("You are using a Customer Email");
    }

    public void deleteUser(UserModel user, DeleteDto deleteDto) {
        if (!BCrypt.checkpw(deleteDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect Password");
        }
        if (!deleteDto.getCheckingString().equals("DEACTIVATE")){
            throw new RuntimeException("Incorrect Checking String");
        }

        deactivateCustomerAccount(user);
    }


    public void deactivateOwnerAccount(UserModel owner, DeleteDto deleteDto) {
        if (!BCrypt.checkpw(deleteDto.getPassword(), owner.getPassword())) {
            throw new RuntimeException("Incorrect Password");
        }
        if (!deleteDto.getCheckingString().equals("DEACTIVATE")){
            throw new RuntimeException("Incorrect Checking String");
        }

        deactivateOwnerAccount(owner);
    }

    @Transactional
    public void deactivateUserByAdmin(UserModel user) {
        if (user.getRole() == Role.OWNER) {
            deactivateOwnerAccount(user);
        } else {
            deactivateCustomerAccount(user);
        }
    }

    @Transactional
    public void activateUserByAdmin(UserModel user) {
        if (user.getRole() == Role.OWNER) {
            activateOwnerAccount(user);
        } else {
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
        }
    }

    @Transactional
    public void deactivateCustomerAccount(UserModel user) {
        user.setStatus(Status.DEACTIVATED);
        reservationService.cancelReservationsByCustomer(user.getId());
        userRepository.save(user);
    }

    @Transactional
    public void deactivateOwnerAccount(UserModel owner) {
        suspendOwnerRestaurantsAndCancelReservations(owner);
        owner.setStatus(Status.DEACTIVATED);
        userRepository.save(owner);
    }

    @Transactional
    public void activateOwnerAccount(UserModel owner) {
        owner.setStatus(Status.ACTIVE);
        userRepository.save(owner);

        List<RestaurantModel> restaurants = restaurantService.getAllRestaurantsOfOwner(owner);
        for (RestaurantModel restaurant : restaurants) {
            if (restaurant.getStatus() == com.example.restaurantTableReservation.Restaurant_Mangement.Status.SUSPENDED) {
                restaurant.setStatus(com.example.restaurantTableReservation.Restaurant_Mangement.Status.ACTIVE);
                restaurantService.saveRestaurant(restaurant);
            }
        }
    }

    private void suspendOwnerRestaurantsAndCancelReservations(UserModel owner) {
        List<RestaurantModel> restaurants = restaurantService.getAllRestaurantsOfOwner(owner);

        for (RestaurantModel restaurant : restaurants) {
            if (restaurant.getStatus() != com.example.restaurantTableReservation.Restaurant_Mangement.Status.DEACTIVATED) {
                reservationService.cancelReservationsByRestaurant(
                        restaurant.getId(),
                        OWNER_DEACTIVATION_CANCEL_MSG
                );
                restaurant.setStatus(com.example.restaurantTableReservation.Restaurant_Mangement.Status.SUSPENDED);
                restaurantService.saveRestaurant(restaurant);
            }
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

}
