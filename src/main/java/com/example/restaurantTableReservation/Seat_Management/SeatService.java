package com.example.restaurantTableReservation.Seat_Management;

import com.example.restaurantTableReservation.Reservation_Management.ReservationModel;
import com.example.restaurantTableReservation.Reservation_Management.ReservationRepository;
import com.example.restaurantTableReservation.Reservation_Management.Status;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantScheduleModel;
import com.example.restaurantTableReservation.Seat_Management.model.SeatModel;
import com.example.restaurantTableReservation.Seat_Management.repository.SeatRepository;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantRepository;
import com.example.restaurantTableReservation.Seat_Management.dto.SeatCheckingForm;
import com.example.restaurantTableReservation.Seat_Management.dto.TimeSlotDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SeatService {

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ReservationRepository reservationRepository;


    public RestaurantModel fetchRestaurant(Long id) {
        RestaurantModel restaurant = restaurantRepository.findById(id).orElse(null);
        if (restaurant == null || restaurant.getStatus() == com.example.restaurantTableReservation.Restaurant_Mangement.Status.DEACTIVATED) {
            return null;
        }
        return restaurant;
    }

    public void saveOrUpdateSeat(RestaurantModel restaurant, int totalSeats, int maxPartySize) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant is required");
        }

        SeatModel seat = restaurant.getSeat();
        if (seat == null) {
            seat = seatRepository.findByRestaurant(restaurant).orElseGet(SeatModel::new);
        }
        seat.setRestaurant(restaurant);
        seat.setTotalSeats(totalSeats);
        seat.setMaxPartySize(maxPartySize);

        SeatModel savedSeat = seatRepository.save(seat);
        restaurant.setSeat(savedSeat);
    }

    public int resolveTotalSeats(RestaurantModel restaurant) {
        SeatModel seatModel = seatRepository.findByRestaurant(restaurant).orElse(null);
        if (seatModel != null) {
            return seatModel.getTotalSeats();
        }
        return restaurant.getTotalSeats();
    }

    public int resolveMaxPartySize(RestaurantModel restaurant) {
        SeatModel seatModel = seatRepository.findByRestaurant(restaurant).orElse(null);
        if (seatModel != null) {
            return seatModel.getMaxPartySize();
        }
        int fromRestaurant = restaurant.getMaxPartySize();
        return fromRestaurant > 0 ? fromRestaurant : 1;
    }

    public List<TimeSlotDTO> getAvailableSlots(
            SeatCheckingForm seatCheckingForm,
            RestaurantModel restaurant) {

        // ── FETCH SEAT MODEL ──────────────────────────────
        // Get the seat details from Seat Management for this restaurant
        // If no seat record exists, fallback to restaurant's legacy values and auto-create
        SeatModel seatModel = seatRepository.findByRestaurant(restaurant).orElse(null);
        int totalSeats;

        if (seatModel != null) {
            totalSeats = seatModel.getTotalSeats();
        } else {
            // Fallback for restaurants created before Seat Management model
            // Auto-create seat record from restaurant's legacy values
            totalSeats = restaurant.getTotalSeats();
            if (totalSeats > 0) {
                saveOrUpdateSeat(restaurant, totalSeats, restaurant.getMaxPartySize());
            } else {
                return Collections.emptyList();  // Can't proceed without seat count
            }
        }

        // ── STEP 1 ───────────────────────────────────────
        // Determine the day-of-week for the requested date
        DayOfWeek dayOfWeek = seatCheckingForm.getDate().getDayOfWeek();

        // ── STEP 2 ───────────────────────────────────────
        // Find the schedule for that specific day
        // restaurant.getSchedules() is already loaded (eager or via join)
        RestaurantScheduleModel schedule = restaurant.getSchedules()
                .stream()
                .filter(s -> s.getDay() == dayOfWeek)
                .findFirst()
                .orElse(null);

        // ── STEP 3 ───────────────────────────────────────
        // If no schedule found OR restaurant is closed that day → return empty
        if (schedule == null || schedule.isClosed()) {
            return Collections.emptyList();
        }

        // ── STEP 4 ───────────────────────────────────────
        // Load ALL confirmed reservations for that day
        List<ReservationModel> dayReservations =
                reservationRepository.findByRestaurantIdAndDateAndStatus(
                        restaurant.getId(),
                        seatCheckingForm.getDate(),
                        Status.CONFIRMED
                );

        // ── STEP 5 ───────────────────────────────────────
        // Derive times from the schedule (not the restaurant root object)
        LocalTime openTime   = schedule.getOpenTime();
        LocalTime closeTime  = schedule.getCloseTime();

        // Last seating = closeTime minus the booking duration
        // so the slot always ends on or before closing
        int duration      = seatCheckingForm.getDuration();
        LocalTime lastSeating   = closeTime.minusMinutes(60);

        int interval        = 30;
        int requestedSeats  = seatCheckingForm.getNoOfGuests();
        LocalDate requestDate = seatCheckingForm.getDate();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        boolean isToday = requestDate != null && requestDate.equals(today);

        LocalTime cursor    = openTime;
        List<TimeSlotDTO> allSlots = new ArrayList<>();

        // ── STEP 6 ───────────────────────────────────────
        // Walk cursor from openTime → lastSeating (inclusive)
        while (!cursor.isAfter(lastSeating)) {

            LocalTime slotStart = cursor;
            LocalTime slotEnd   = cursor.plusMinutes(duration);

            // Guard: midnight wrap or overrun
            if (slotEnd.isBefore(slotStart) || slotEnd.isAfter(closeTime)) {
                cursor = cursor.plusMinutes(interval);
                continue;
            }

            // ── STEP 7 ─────────────────────────────────
            // Count overlapping confirmed seats
            int seatsBooked = 0;
            for (ReservationModel r : dayReservations) {
                boolean startsBeforeEnd = r.getStartingTime().isBefore(slotEnd);
                boolean endsAfterStart  = r.getEndingTime().isAfter(slotStart);
                if (startsBeforeEnd && endsAfterStart) {
                    seatsBooked += r.getNoOfSeats();
                }
            }

            // ── STEP 8 ─────────────────────────────────
            int seatsLeft = Math.max(0, totalSeats - seatsBooked);
            boolean pastSlot = isToday && !slotStart.isAfter(now);
            boolean hasCapacity = seatsLeft >= requestedSeats;
            boolean available = !pastSlot && hasCapacity;

            allSlots.add(new TimeSlotDTO(slotStart, slotEnd, seatsLeft, available, pastSlot));

            cursor = cursor.plusMinutes(interval);
        }

        return allSlots;
    }

}
