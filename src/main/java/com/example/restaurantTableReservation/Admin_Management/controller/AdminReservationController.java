package com.example.restaurantTableReservation.Admin_Management.controller;

/*
 * AdminReservationController — view and cancel reservations platform-wide.
 *
 * GET /admin/reservations — filters: keyword, status, date, timeFilter, view (daily|monthly|yearly).
 *   Resolves effective date range from view + date param; passes list to reservation-management.html.
 * POST /admin/reservations/{id}/cancel — sets reservation status to cancelled.
 */

import com.example.restaurantTableReservation.Admin_Management.util.AdminFilterHelper;
import com.example.restaurantTableReservation.Reservation_Management.ReservationModel;
import com.example.restaurantTableReservation.Reservation_Management.ReservationRepository;
import com.example.restaurantTableReservation.Reservation_Management.Status;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

@Controller
public class AdminReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/admin/reservations")
    public String reservationManagementPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false, defaultValue = "daily") String view,
            Model model,
            HttpSession session
    ) {

        String normalizedView = normalizeView(view);
        List<ReservationModel> reservations = reservationRepository.findAll();

        if (AdminFilterHelper.hasFilterValue(keyword)) {
            String search = keyword.trim().toLowerCase();
            reservations = reservations.stream()
                    .filter(r -> matchesReservationSearch(r, search))
                    .toList();
        }

        if (AdminFilterHelper.hasFilterValue(status)) {
            reservations = reservations.stream()
                    .filter(r -> r.getStatus().name().equalsIgnoreCase(status.trim()))
                    .toList();
        }

        String effectiveDate = resolveEffectiveDate(date, normalizedView);
        final String dateFilterValue = effectiveDate;
        final String dateView = normalizedView;

        if (AdminFilterHelper.hasFilterValue(dateFilterValue)) {
            reservations = reservations.stream()
                    .filter(r -> matchesDateFilter(r, dateFilterValue.trim(), dateView))
                    .toList();
        }

        if (AdminFilterHelper.hasFilterValue(timeFilter)) {
            reservations = reservations.stream()
                    .filter(r -> matchesTimeFilter(r, timeFilter.trim().toLowerCase()))
                    .toList();
        }

        List<ReservationModel> allReservations = reservationRepository.findAll();
        LocalDate today = LocalDate.now();

        long todayReservations = allReservations.stream()
                .filter(r -> r.getDate() != null && r.getDate().equals(today))
                .count();

        long confirmedReservations = allReservations.stream()
                .filter(r -> r.getStatus() == Status.CONFIRMED)
                .count();

        long cancelledReservations = allReservations.stream()
                .filter(r -> r.getStatus() == Status.CANCELLED)
                .count();

        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("date", effectiveDate != null ? effectiveDate : "");
        model.addAttribute("timeFilter", timeFilter != null ? timeFilter : "");
        model.addAttribute("view", normalizedView);
        model.addAttribute("loggedAdmin", session.getAttribute("loggedAdmin"));
        model.addAttribute("reservations", reservations);
        model.addAttribute("totalReservations", reservationRepository.count());
        model.addAttribute("confirmedReservations", confirmedReservations);
        model.addAttribute("cancelledReservations", cancelledReservations);
        model.addAttribute("todayReservations", todayReservations);

        return "AdminManagement/reservation-management";
    }

    private String resolveEffectiveDate(String date, String view) {
        if (AdminFilterHelper.hasFilterValue(date)) {
            return date;
        }
        return switch (view) {
            case "monthly" -> YearMonth.now().toString();
            case "yearly" -> String.valueOf(Year.now().getValue());
            default -> LocalDate.now().toString();
        };
    }

    private String normalizeView(String view) {
        if (view == null) {
            return "daily";
        }
        String v = view.trim().toLowerCase();
        if ("monthly".equals(v) || "yearly".equals(v)) {
            return v;
        }
        return "daily";
    }

    private boolean matchesDateFilter(ReservationModel reservation, String dateValue, String view) {
        if (reservation.getDate() == null) {
            return false;
        }
        return switch (view) {
            case "monthly" -> {
                YearMonth yearMonth = YearMonth.parse(dateValue);
                yield reservation.getDate().getYear() == yearMonth.getYear()
                        && reservation.getDate().getMonth() == yearMonth.getMonth();
            }
            case "yearly" -> {
                int year = Integer.parseInt(dateValue);
                yield reservation.getDate().getYear() == year;
            }
            default -> reservation.getDate().equals(LocalDate.parse(dateValue));
        };
    }

    private boolean matchesReservationSearch(ReservationModel reservation, String search) {
        if (reservation.getCustomer() != null) {
            String customerName = (reservation.getCustomer().getFirstName() + " "
                    + reservation.getCustomer().getLastName()).toLowerCase();
            if (customerName.contains(search)) {
                return true;
            }
            if (reservation.getCustomer().getEmail() != null
                    && reservation.getCustomer().getEmail().toLowerCase().contains(search)) {
                return true;
            }
            if (reservation.getCustomer().getPhoneNumber() != null
                    && reservation.getCustomer().getPhoneNumber().contains(search)) {
                return true;
            }
        }
        if (reservation.getRestaurant() != null
                && reservation.getRestaurant().getRestaurantName() != null
                && reservation.getRestaurant().getRestaurantName().toLowerCase().contains(search)) {
            return true;
        }
        return reservation.getReservationCode() != null
                && reservation.getReservationCode().toLowerCase().contains(search);
    }

    private boolean matchesTimeFilter(ReservationModel reservation, String timeFilter) {
        if (reservation.getStartingTime() == null) {
            return false;
        }
        int hour = reservation.getStartingTime().getHour();
        return switch (timeFilter) {
            case "breakfast" -> hour >= 6 && hour < 11;
            case "lunch" -> hour >= 11 && hour < 17;
            case "dinner" -> hour >= 17 && hour < 23;
            default -> true;
        };
    }

    @PostMapping("/admin/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id) {

        ReservationModel reservation = reservationRepository.findById(id).orElseThrow();
        reservation.setStatus(Status.CANCELLED);
        reservationRepository.save(reservation);

        return "redirect:/admin/reservations";
    }
}
