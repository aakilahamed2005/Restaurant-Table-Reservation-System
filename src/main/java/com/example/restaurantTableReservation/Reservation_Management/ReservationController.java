package com.example.restaurantTableReservation.Reservation_Management;

import com.example.restaurantTableReservation.Restaurant_Mangement.RestaurantService;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Seat_Management.SeatService;
import com.example.restaurantTableReservation.Seat_Management.dto.SeatCheckingForm;
import com.example.restaurantTableReservation.Seat_Management.dto.TimeSlotDTO;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.Comparator;




@Controller
public class ReservationController {

    @Autowired
    ReservationService reservationService;
    @Autowired
    private RestaurantService restaurantService;
    @Autowired
    private SeatService seatService;

    // CUSTOMER = online reservation
    @PostMapping("/reservation-save")
    public String createReservation(ReservationSlotDTO reservationSlotDTO,
                                    HttpSession session, Model model) {
        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in";
        }

        RestaurantModel restaurant = restaurantService.getRestaurantById(reservationSlotDTO.getRestaurantId());
        if (restaurant == null) {
            return "redirect:/restaurant-listing";
        }

        int maxPartySize = seatService.resolveMaxPartySize(restaurant);
        if (reservationSlotDTO.getNoOfGuest() == null || reservationSlotDTO.getNoOfGuest() < 1) {
            reservationSlotDTO.setNoOfGuest(1);
        } else if (reservationSlotDTO.getNoOfGuest() > maxPartySize) {
            reservationSlotDTO.setNoOfGuest(maxPartySize);
        }

        SeatCheckingForm seatCheckingForm = new SeatCheckingForm();
        seatCheckingForm.setRestaurantId(reservationSlotDTO.getRestaurantId());
        seatCheckingForm.setDate(reservationSlotDTO.getDate());
        seatCheckingForm.setNoOfGuests(reservationSlotDTO.getNoOfGuest());
        seatCheckingForm.setDuration(
                reservationSlotDTO.getRequestedDuration() != null ? reservationSlotDTO.getRequestedDuration() : 120
        );

        List<TimeSlotDTO> slots = seatService.getAvailableSlots(seatCheckingForm, restaurant);
        boolean slotStillAvailable = slots.stream().anyMatch(slot ->
                slot.getStartTime().equals(reservationSlotDTO.getStartingTime()) && slot.isAvailable());


        // Get the currently logged-in user from the session to make sure only logged-in customers can reserve.
        if (!slotStillAvailable) {
            model.addAttribute("error", "That time slot is no longer available. Please choose another time.");
            model.addAttribute("slots", slots);
            model.addAttribute("restaurant", restaurant);
            model.addAttribute("user", user);
            model.addAttribute("seatCheckingForm", seatCheckingForm);
            model.addAttribute("customerFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            model.addAttribute("today", LocalDate.now().toString());
            model.addAttribute("maxPartySize", maxPartySize);
            model.addAttribute("totalSeats", seatService.resolveTotalSeats(restaurant));
            model.addAttribute("reservationSlotDTO", reservationSlotDTO);
            return "ReservationAndSeatManagement/restaurant-seat-availability";
        }


        // Save the reservation and pass the saved details to the success page.
        ReservationModel reservation = reservationService.saveReservation(reservationSlotDTO);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("reservation", reservation);
        model.addAttribute("customerFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("restaurantNameFirstLetter", restaurant.getRestaurantName().charAt(0));
        model.addAttribute("user", user);



        //springboot loads into thymleaf
        return "ReservationAndSeatManagement/reservation-success";
    }

    //CUSTOMER = booking history
    @GetMapping("/customer-booking-records")
    public String viewCustomerReservations(HttpSession session, Model model) {

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in";
        }

        List<ReservationModel> reservations =
                reservationService.getReservationsByCustomer(user.getId())
                        .stream()
                        .sorted(
                                Comparator.comparing(ReservationModel::getDate)
                                        .thenComparing(ReservationModel::getStartingTime)
                                        .reversed()
                        )
                        .toList();


        LocalDate today = LocalDate.now();

        List<ReservationModel> upcomingReservations = reservations.stream()
                .filter(res -> res.getStatus() == Status.CONFIRMED)
                .filter(res -> !res.getDate().isBefore(today))
                .toList();

        List<ReservationModel> completedReservations = reservations.stream()
                .filter(res -> res.getStatus() == Status.CONFIRMED)
                .filter(res -> res.getDate().isBefore(today))
                .toList();

        List<ReservationModel> cancelledReservations = reservations.stream()
                .filter(res -> res.getStatus() == Status.CANCELLED)
                .toList();

        model.addAttribute("reservations", reservations);
        model.addAttribute("upcomingReservations", upcomingReservations);
        model.addAttribute("completedReservations", completedReservations);
        model.addAttribute("cancelledReservations", cancelledReservations);

        model.addAttribute("allCount", reservations.size());
        model.addAttribute("upcomingCount", upcomingReservations.size());
        model.addAttribute("completedCount", completedReservations.size());
        model.addAttribute("cancelledCount", cancelledReservations.size());

        model.addAttribute("today", today);

        model.addAttribute("user", user);
        model.addAttribute("customerFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));

        return "ReservationAndSeatManagement/customer-booking-records";
    }



    //CUSTOMER = booking cancellation
     @PostMapping("/customer/reservation/cancel/{id}")
    public String cancelCustomerReservation(@PathVariable Long id,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in";
        }

        try {
            reservationService.cancelReservationByCustomer(
                    id,
                    user.getId(),
                    "Cancelled by you. Your reservation has been removed from the schedule."
            );
            redirectAttributes.addFlashAttribute("successMessage", "Reservation cancelled successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/customer-booking-records";
    }

    @GetMapping("/customer-booking-cancel")
    public String bookingCancelledPage() {
        return "ReservationAndSeatManagement/customer-booking-cancel";
    }



    // RESTAURANT OWNER - View Reservations
    @GetMapping("/owner-restaurant-reservations/{restaurantId}")
    public String viewRestaurantReservations(@PathVariable Long restaurantId,
                                            HttpSession session,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        // Verify restaurant ownership
        RestaurantModel restaurant = restaurantService.getRestaurantById(restaurantId);
        if (restaurant == null || !restaurant.getOwner().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/owner-restaurants";
        }

        List<ReservationModel> allReservations =
                reservationService.getReservationsByRestaurant(restaurantId)
                        .stream()
                        .sorted(Comparator.comparing(ReservationModel::getDate)
                                .thenComparing(ReservationModel::getStartingTime)
                                .reversed())
                        .toList();

        LocalDate today = LocalDate.now();

        List<ReservationModel> upcomingReservations = allReservations.stream()
                .filter(res -> res.getStatus() == Status.CONFIRMED)
                .filter(res -> !res.getDate().isBefore(today))
                .toList();

        List<ReservationModel> completedReservations = allReservations.stream()
                .filter(res -> res.getStatus() == Status.CONFIRMED)
                .filter(res -> res.getDate().isBefore(today))
                .toList();

        List<ReservationModel> cancelledReservations = allReservations.stream()
                .filter(res -> res.getStatus() == Status.CANCELLED)
                .toList();

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("reservations", allReservations);
        model.addAttribute("upcomingReservations", upcomingReservations);
        model.addAttribute("completedReservations", completedReservations);
        model.addAttribute("cancelledReservations", cancelledReservations);

        model.addAttribute("allCount", allReservations.size());
        model.addAttribute("upcomingCount", upcomingReservations.size());
        model.addAttribute("completedCount", completedReservations.size());
        model.addAttribute("cancelledCount", cancelledReservations.size());

        model.addAttribute("today", today);
        model.addAttribute("user", user);
        model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));

        return "ReservationAndSeatManagement/owner-restaurant-reservations";
    }

    // RESTAURANT OWNER - Cancel Reservation
    @PostMapping("/owner/reservation/cancel/{reservationId}/{restaurantId}")
    public String cancelOwnerReservation(@PathVariable Long reservationId,
                                        @PathVariable Long restaurantId,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        try {
            RestaurantModel restaurant = restaurantService.getRestaurantById(restaurantId);
            if (restaurant == null || !restaurant.getOwner().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized access");
            }

            reservationService.cancelReservationByRestaurantOwner(
                    reservationId,
                    restaurantId,
                    "Cancelled by the restaurant owner."
            );
            redirectAttributes.addFlashAttribute("cancelSuccess", "Reservation cancelled successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/owner-restaurant-reservations/" + restaurantId;
    }

    // RESTAURANT OWNER - Walk-in Reservation Management Page
    @GetMapping("/owner-walk-in-reservations/{restaurantId}")
    public String viewWalkInReservations(@PathVariable Long restaurantId,
                                        HttpSession session,
                                        Model model,
                                        RedirectAttributes redirectAttributes,
                                        @org.springframework.web.bind.annotation.RequestParam(required = false) String date,
                                        @org.springframework.web.bind.annotation.RequestParam(required = false) Integer noOfGuests,
                                        @org.springframework.web.bind.annotation.RequestParam(required = false) Integer duration) {

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        // Verify restaurant ownership
        RestaurantModel restaurant = restaurantService.getRestaurantById(restaurantId);
        if (restaurant == null || !restaurant.getOwner().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/owner-restaurants";
        }

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("user", user);
        model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("firstLetter", user.getFirstName().charAt(0));
        model.addAttribute("today", LocalDate.now());
        SeatCheckingForm seatCheckingForm = new SeatCheckingForm();
        seatCheckingForm.setRestaurantId(restaurantId);
        seatCheckingForm.setCustomerId(user.getId());
        seatCheckingForm.setDate(LocalDate.now());
        seatCheckingForm.setNoOfGuests(2);
        seatCheckingForm.setDuration(120);

        // If optional query params provided, use them to pre-populate the search and compute slots
        List<TimeSlotDTO> slots = List.of();
        if (date != null) {
            seatCheckingForm.setDate(LocalDate.parse(date));
        }
        if (noOfGuests != null) {
            seatCheckingForm.setNoOfGuests(noOfGuests);
        }
        if (duration != null) {
            seatCheckingForm.setDuration(duration);
        }

        // Only compute slots if the controller was invoked with explicit query params (date provided)
        int bookedSeats = 0;
        if (date != null) {
            slots = seatService.getAvailableSlots(seatCheckingForm, restaurant);

            bookedSeats = reservationService.getReservationsByRestaurant(restaurantId)
                    .stream()
                    .filter(r -> r.getStatus() == Status.CONFIRMED)
                    .filter(r -> r.getDate().isEqual(seatCheckingForm.getDate()))
                    .mapToInt(ReservationModel::getNoOfSeats)
                    .sum();
        }

        model.addAttribute("seatCheckingForm", seatCheckingForm);
        model.addAttribute("walkInReservationDTO", new WalkInReservationDTO());
        model.addAttribute("slots", slots);
        model.addAttribute("bookedSeats", bookedSeats);

        List<ReservationModel> walkIns = reservationService.getReservationsByRestaurant(restaurantId)
                .stream()
                .filter(reservation -> reservation.getReservationType() == ReservationType.WALK_IN)
                .sorted(Comparator.comparing(ReservationModel::getDate)
                        .thenComparing(ReservationModel::getStartingTime)
                        .reversed())
                .toList();

        model.addAttribute("walkInReservations", walkIns);

        return "ReservationAndSeatManagement/owner-walk-in-reservations";
    }


    //RESTAURANT OWNER = Booking availability(Walk in)
    @PostMapping("/owner-walk-in-reservations/{restaurantId}/availability")
    public String findWalkInAvailability(@PathVariable Long restaurantId,
                                         @ModelAttribute("seatCheckingForm") SeatCheckingForm seatCheckingForm,
                                         HttpSession session,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        try {
            RestaurantModel restaurant = restaurantService.getRestaurantById(restaurantId);
            if (restaurant == null || !restaurant.getOwner().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized access");
            }

            seatCheckingForm.setRestaurantId(restaurantId);
            seatCheckingForm.setCustomerId(user.getId());

            List<TimeSlotDTO> slots = seatService.getAvailableSlots(seatCheckingForm, restaurant);

            int bookedSeats = reservationService.getReservationsByRestaurant(restaurantId)
                    .stream()
                    .filter(r -> r.getStatus() == Status.CONFIRMED)
                    .filter(r -> r.getDate().isEqual(seatCheckingForm.getDate()))
                    .mapToInt(ReservationModel::getNoOfSeats)
                    .sum();

            model.addAttribute("restaurant", restaurant);
            model.addAttribute("user", user);
            model.addAttribute("userFullName", user.getFirstName() + " " + user.getLastName());
            model.addAttribute("firstLetter", user.getFirstName().charAt(0));
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("slots", slots);
            model.addAttribute("seatCheckingForm", seatCheckingForm);
            model.addAttribute("walkInReservationDTO", new WalkInReservationDTO());
            model.addAttribute("bookedSeats", bookedSeats);

            List<ReservationModel> walkIns = reservationService.getReservationsByRestaurant(restaurantId)
                    .stream()
                    .filter(reservation -> reservation.getReservationType() == ReservationType.WALK_IN)
                    .sorted(Comparator.comparing(ReservationModel::getDate)
                            .thenComparing(ReservationModel::getStartingTime)
                            .reversed())
                    .toList();
            model.addAttribute("walkInReservations", walkIns);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner-walk-in-reservations/" + restaurantId;
        }

        return "ReservationAndSeatManagement/owner-walk-in-reservations";
    }

    // RESTAURANT OWNER - Create Walk-in Reservation
    @PostMapping("/owner-walk-in-reservations/{restaurantId}/save")
    public String createWalkInReservation(@PathVariable Long restaurantId,
                                          @ModelAttribute("walkInReservationDTO") WalkInReservationDTO walkInReservationDTO,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {

        UserModel user = (UserModel) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/sign-in?tab=partner";
        }

        try {
            RestaurantModel restaurant = restaurantService.getRestaurantById(restaurantId);
            if (restaurant == null || !restaurant.getOwner().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized access");
            }

            ReservationModel reservation = reservationService.createWalkInReservation(restaurantId, walkInReservationDTO);
            redirectAttributes.addFlashAttribute("success", "Walk-in reservation " + reservation.getReservationCode() + " created successfully");
            // Redirect back to page with same search params so owner sees updated availability
            String d = walkInReservationDTO.getDate() != null ? walkInReservationDTO.getDate().toString() : null;
            Integer guests = walkInReservationDTO.getNoOfGuests();
            LocalDate dt = walkInReservationDTO.getDate();
            Integer dur = null;
            if (walkInReservationDTO.getStartingTime() != null && walkInReservationDTO.getEndingTime() != null) {
                // derive duration in minutes
                dur = (int) java.time.Duration.between(walkInReservationDTO.getStartingTime(), walkInReservationDTO.getEndingTime()).toMinutes();
            }
            String redirectUrl = "/owner-walk-in-reservations/" + restaurantId;
            if (d != null && guests != null && dur != null) {
                redirectUrl += "?date=" + d + "&noOfGuests=" + guests + "&duration=" + dur;
            }
            return "redirect:" + redirectUrl;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/owner-walk-in-reservations/" + restaurantId;
    }

}

