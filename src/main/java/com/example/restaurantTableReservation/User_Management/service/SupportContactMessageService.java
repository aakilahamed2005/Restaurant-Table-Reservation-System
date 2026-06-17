package com.example.restaurantTableReservation.User_Management.service;

import com.example.restaurantTableReservation.Restaurant_Mangement.model.RestaurantModel;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.dto.RestaurantSupportContactRequestDto;
import com.example.restaurantTableReservation.User_Management.dto.SupportContactRequestDto;
import com.example.restaurantTableReservation.User_Management.model.SupportContactMessage;
import com.example.restaurantTableReservation.User_Management.repository.SupportContactMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class SupportContactMessageService {

    public static final String SOURCE_USER_ACCOUNT = "USER_ACCOUNT";
    public static final String SOURCE_RESTAURANT_LISTING = "RESTAURANT_LISTING";

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "DEACTIVATED_ACCOUNT",
            "SUSPENDED_RESTAURANT",
            "RESERVATION_ISSUE",
            "ACCOUNT_ACCESS",
            "OTHER"
    );

    private final SupportContactMessageRepository repository;

    public SupportContactMessageService(SupportContactMessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveUserAccountMessage(SupportContactRequestDto dto) {
        String category = normalizeCategory(dto.getCategory());
        String email = requireEmail(dto.getEmail());
        String description = requireDescription(dto.getDescription());

        SupportContactMessage m = new SupportContactMessage();
        m.setCategory(category);
        m.setEmail(email);
        m.setDescription(description);
        m.setMessageSource(SOURCE_USER_ACCOUNT);
        m.setAttemptedLoginEmail(trimToNull(dto.getAttemptedLoginEmail()));
        m.setAdminRead(false);
        repository.save(m);
    }

    @Transactional
    public void saveRestaurantListingMessage(UserModel owner, RestaurantModel restaurant, RestaurantSupportContactRequestDto dto) {
        String category = normalizeCategory(dto.getCategory());
        String email = requireEmail(dto.getEmail());
        String description = requireDescription(dto.getDescription());

        SupportContactMessage m = new SupportContactMessage();
        m.setCategory(category);
        m.setEmail(email);
        m.setDescription(description);
        m.setMessageSource(SOURCE_RESTAURANT_LISTING);
        m.setRestaurantId(restaurant.getId());
        m.setRestaurantName(restaurant.getRestaurantName());
        m.setOwnerUserId(owner.getId());
        m.setAdminRead(false);
        repository.save(m);
    }

    public List<SupportContactMessage> findAllNewestFirst() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public long countUnread() {
        return repository.countByAdminReadFalse();
    }

    @Transactional
    public void markAdminRead(Long id) {
        SupportContactMessage m = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Message not found"));
        m.setAdminRead(true);
        repository.save(m);
    }

    @Transactional
    public void markAllAdminRead() {
        List<SupportContactMessage> unread = repository.findAllByAdminReadFalse();
        for (SupportContactMessage m : unread) {
            m.setAdminRead(true);
        }
        repository.saveAll(unread);
    }

    @Transactional
    public void deleteMessage(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Message not found");
        }
        repository.deleteById(id);
    }

    @Transactional
    public void deleteAllMessages() {
        repository.deleteAll();
    }

    private String normalizeCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Please choose a topic.");
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_CATEGORIES.contains(key)) {
            throw new IllegalArgumentException("Invalid topic selected.");
        }
        return key;
    }

    private String requireEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String trimmed = email.trim();
        if (trimmed.length() > 255 || !trimmed.contains("@")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        return trimmed;
    }

    private String requireDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Please describe your issue.");
        }
        String trimmed = description.trim();
        if (trimmed.length() > 4000) {
            throw new IllegalArgumentException("Description is too long (max 4000 characters).");
        }
        return trimmed;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
