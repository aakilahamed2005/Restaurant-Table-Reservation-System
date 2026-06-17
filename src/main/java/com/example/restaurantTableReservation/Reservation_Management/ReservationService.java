package com.example.restaurantTableReservation.Reservation_Management;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.Restaurant_Mangement.repository.RestaurantRepository;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    public ReservationModel saveReservation(ReservationSlotDTO reservationSlotDTO) {
        RestaurantModel restaurant = restaurantRepository.findById(reservationSlotDTO.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        UserModel user = userRepository.findById(reservationSlotDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        ReservationModel reservation = new ReservationModel();

        //maps DTO into ReservationModel
        reservation.setRestaurant(restaurant);
        reservation.setCustomer(user);
        reservation.setDate(reservationSlotDTO.getDate());
        reservation.setStartingTime(reservationSlotDTO.getStartingTime());
        reservation.setEndingTime(reservationSlotDTO.getEndingTime());
        reservation.setNoOfSeats(reservationSlotDTO.getNoOfGuest());
        reservation.setStatus(Status.CONFIRMED);
        reservation.setReservationType(ReservationType.ONLINE);
        reservation.setCustomerName(user.getFirstName() + " " + user.getLastName());
        reservation.setCustomerPhoneNumber(user.getPhoneNumber());
        reservation.setCustomerEmail(user.getEmail());
        reservation.setNotes(null);

        reservation = reservationRepository.save(reservation);

        // Generate formatted code
        String code = String.format("RES-%03d-%03d-%06d",
                reservation.getRestaurant().getId(),
                reservation.getCustomer().getId(),
                reservation.getId()
        );

        reservation.setReservationCode(code);
        return reservationRepository.save(reservation);
    }

    public List<ReservationModel> getReservationsByCustomer(Long customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }


    public ReservationModel cancelReservationByCustomer(Long reservationId, Long customerId) {
        return cancelReservationByCustomer(reservationId, customerId, "Cancelled by the customer.");
    }

    public ReservationModel cancelReservationByCustomer(Long reservationId, Long customerId, String cancellationReason) {

        ReservationModel reservation =
                reservationRepository.findByIdAndCustomerId(reservationId, customerId);

        if (reservation == null) {
            throw new RuntimeException("Reservation not found or unauthorized action");
        }

        reservation.setStatus(Status.CANCELLED);
        reservation.setCancellationReason(normalizeCancellationReason(cancellationReason, "Cancelled by the customer."));

        return reservationRepository.save(reservation);
    }

    public void deleteReservationOfUser(UserModel user){
         reservationRepository.deleteByCustomer(user);

     }


     //When deactivated
    @Transactional
    public void cancelReservationsByCustomer(Long customerId) {
        cancelReservationsByCustomer(customerId,
                "This account has been deactivated, so your reservation was automatically cancelled. We’re sorry for the inconvenience and hope you’ll book again soon.");
    }

    @Transactional
    public void cancelReservationsByCustomer(Long customerId, String cancellationReason) {
        List<ReservationModel> reservations = reservationRepository.findByCustomerId(customerId);
        String resolvedReason = normalizeCancellationReason(cancellationReason,
                "This account has been deactivated, so your reservation was automatically cancelled.");
        List<ReservationModel> toCancel = reservations.stream()
                .filter(r -> r.getStatus() != Status.CANCELLED)
                .collect(Collectors.toList());

        if (!toCancel.isEmpty()) {
            toCancel.forEach(r -> {
                r.setStatus(Status.CANCELLED);
                r.setCancellationReason(resolvedReason);
            });
            reservationRepository.saveAll(toCancel);
        }
    }


    public List<ReservationModel> getReservationsByRestaurant(Long restaurantId) {
        return reservationRepository.findByRestaurantId(restaurantId);
    }

    public ReservationModel cancelReservationByRestaurantOwner(Long reservationId, Long restaurantId) {
        return cancelReservationByRestaurantOwner(reservationId, restaurantId, "Cancelled by the restaurant owner.");
    }

    public ReservationModel cancelReservationByRestaurantOwner(Long reservationId, Long restaurantId, String cancellationReason) {
        ReservationModel reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Unauthorized action");
        }

        if (reservation.getStatus() != Status.CONFIRMED) {
            throw new RuntimeException("Only confirmed reservations can be cancelled");
        }

        reservation.setStatus(Status.CANCELLED);
        reservation.setCancellationReason(normalizeCancellationReason(cancellationReason, "Cancelled by the restaurant owner."));
        return reservationRepository.save(reservation);
    }

    // WALK-IN RESERVATION METHODS
    public int getAvailableSeatsForTime(Long restaurantId, LocalDate date, LocalTime time) {
        RestaurantModel restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // Get all confirmed reservations for this date
        List<ReservationModel> dayReservations = reservationRepository
                .findByRestaurantIdAndDateAndStatus(restaurantId, date, Status.CONFIRMED);

        // Calculate booked seats at this time
        int seatsBooked = 0;
        for (ReservationModel reservation : dayReservations) {
            // Check if the current time falls within the reservation window
            boolean timeOverlaps = time.isAfter(reservation.getStartingTime())
                    && time.isBefore(reservation.getEndingTime());
            if (timeOverlaps) {
                seatsBooked += reservation.getNoOfSeats();
            }
        }

        return restaurant.getTotalSeats() - seatsBooked;
    }

    public ReservationModel createWalkInReservation(Long restaurantId,
                                                     WalkInReservationDTO walkInReservationDTO) {
        RestaurantModel restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (walkInReservationDTO.getDate() == null || walkInReservationDTO.getStartingTime() == null) {
            throw new RuntimeException("Date and time are required");
        }

        int availableSeats = getAvailableSeatsForTime(
                restaurantId,
                walkInReservationDTO.getDate(),
                walkInReservationDTO.getStartingTime()
        );

        if (availableSeats < walkInReservationDTO.getNoOfGuests()) {
            throw new RuntimeException("Not enough available seats. Available: " + availableSeats);
        }

        LocalTime endingTime = walkInReservationDTO.getEndingTime();
        if (endingTime == null) {
            endingTime = walkInReservationDTO.getStartingTime().plusHours(2);
        }

        ReservationModel reservation = new ReservationModel();
        reservation.setRestaurant(restaurant);
        reservation.setDate(walkInReservationDTO.getDate());
        reservation.setStartingTime(walkInReservationDTO.getStartingTime());
        reservation.setEndingTime(endingTime);
        reservation.setNoOfSeats(walkInReservationDTO.getNoOfGuests());
        reservation.setStatus(Status.CONFIRMED);
        reservation.setReservationType(ReservationType.WALK_IN);
        // Keep customer_id non-null for the existing schema; walk-ins are owned by the restaurant owner record.
        reservation.setCustomer(restaurant.getOwner());
        reservation.setCustomerName(walkInReservationDTO.getCustomerName());
        reservation.setCustomerPhoneNumber(walkInReservationDTO.getCustomerPhoneNumber());
        reservation.setCustomerEmail(walkInReservationDTO.getCustomerEmail());
        reservation.setNotes(walkInReservationDTO.getNotes());

        reservation = reservationRepository.save(reservation);

        // Generate walk-in code
        String code = String.format("WLK-%03d-%06d-%06d",
                reservation.getRestaurant().getId(),
                reservation.getId(),
                System.currentTimeMillis() % 1000000
        );

        reservation.setReservationCode(code);
        return reservationRepository.save(reservation);
    }


    //Update method not used
    public ReservationModel updateWalkInReservation(Long reservationId, Long restaurantId,
                                                     int noOfGuests, LocalTime endingTime) {
        ReservationModel reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!reservation.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("Unauthorized action");
        }

        if (reservation.getReservationType() != ReservationType.WALK_IN) {
            throw new RuntimeException("Only walk-in reservations can be updated this way");
        }

        reservation.setNoOfSeats(noOfGuests);
        reservation.setEndingTime(endingTime);

        return reservationRepository.save(reservation);
    }

    /**
     * Cancel all confirmed reservations for a restaurant (used when restaurant is removed/deactivated).
     */
    @Transactional
    public void cancelReservationsByRestaurant(Long restaurantId) {
        cancelReservationsByRestaurant(restaurantId,
                "This restaurant is no longer available, so your reservation was automatically cancelled. We’re sorry for the inconvenience and hope you’ll book again soon.");
    }

    @Transactional
    public void cancelReservationsByRestaurant(Long restaurantId, String cancellationReason) {
        List<ReservationModel> reservations = reservationRepository.findByRestaurantId(restaurantId);
        String resolvedReason = normalizeCancellationReason(cancellationReason,
                "This restaurant is no longer available, so your reservation was automatically cancelled. We’re sorry for the inconvenience and hope you’ll book again soon.");
        List<ReservationModel> toCancel = reservations.stream()
                .filter(r -> r.getStatus() != Status.CANCELLED)
                .collect(Collectors.toList());

        if (!toCancel.isEmpty()) {
            toCancel.forEach(r -> {
                r.setStatus(Status.CANCELLED);
                r.setCancellationReason(resolvedReason);
            });
            reservationRepository.saveAll(toCancel);
        }
    }

    private String normalizeCancellationReason(String cancellationReason, String fallbackReason) {
        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            return fallbackReason;
        }
        return cancellationReason.trim();
    }
}
