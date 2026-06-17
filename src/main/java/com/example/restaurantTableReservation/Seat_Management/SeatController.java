package com.example.restaurantTableReservation.Seat_Management;

import com.example.restaurantTableReservation.Reservation_Management.ReservationSlotDTO;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Seat_Management.dto.SeatCheckingForm;
import com.example.restaurantTableReservation.Seat_Management.dto.TimeSlotDTO;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class SeatController {

    @Autowired
    SeatService seatService;

    @GetMapping("/restaurant-seat-checking/{id}")
    public String restaurantSeatChecking(@PathVariable Long id, Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in";
        }

        RestaurantModel restaurant = seatService.fetchRestaurant(id);

        SeatCheckingForm seatCheckingForm = new SeatCheckingForm();
        seatCheckingForm.setRestaurantId(restaurant.getId());
        seatCheckingForm.setCustomerId(user.getId());
        
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("user", user);
        model.addAttribute("seatCheckingForm", seatCheckingForm);

        model.addAttribute("customerFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("restaurantNameFirstLetter", restaurant.getRestaurantName().charAt(0));
        model.addAttribute("restaurantId", restaurant.getId());
        model.addAttribute("today",  LocalDate.now().toString());
        model.addAttribute("maxPartySize", seatService.resolveMaxPartySize(restaurant));
        model.addAttribute("totalSeats", seatService.resolveTotalSeats(restaurant));


        return "ReservationAndSeatManagement/restaurant-seat-checking";
    }

    @PostMapping("/restaurant-seat-availability")
    public String restaurantSeatAvailability(@ModelAttribute("seatCheckingForm") SeatCheckingForm seatCheckingForm,
                                             Model model, HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in";
        }

        RestaurantModel restaurant = seatService.fetchRestaurant(seatCheckingForm.getRestaurantId());
        int maxPartySize = seatService.resolveMaxPartySize(restaurant);
        if (seatCheckingForm.getNoOfGuests() > maxPartySize) {
            seatCheckingForm.setNoOfGuests(maxPartySize);
        }
        if (seatCheckingForm.getNoOfGuests() < 1) {
            seatCheckingForm.setNoOfGuests(1);
        }

        List<TimeSlotDTO> slots = seatService.getAvailableSlots(seatCheckingForm, restaurant);

        model.addAttribute("slots", slots);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("user", user);
        model.addAttribute("seatCheckingForm", seatCheckingForm);
        model.addAttribute("customerFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("today",  LocalDate.now().toString());
        model.addAttribute("maxPartySize", maxPartySize);
        model.addAttribute("totalSeats", seatService.resolveTotalSeats(restaurant));

        ReservationSlotDTO reservationSlotDTO = new ReservationSlotDTO();

        reservationSlotDTO.setNoOfGuest(seatCheckingForm.getNoOfGuests());
        model.addAttribute("reservationSlotDTO", reservationSlotDTO);

        return "ReservationAndSeatManagement/restaurant-seat-availability";
    }
}


