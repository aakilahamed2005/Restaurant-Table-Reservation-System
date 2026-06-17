package com.example.restaurantTableReservation.Admin_Management.controller;

/*
 * REST JSON API for the admin notification bell (support / contact messages).
 * All endpoints require session attribute "loggedAdmin" or return 401.
 *
 * GET    /admin/api/support-messages           — list newest first
 * POST   /admin/api/support-messages/{id}/read — mark one read
 * POST   /admin/api/support-messages/read-all    — mark all read
 * DELETE /admin/api/support-messages/{id}      — delete one
 * DELETE /admin/api/support-messages           — delete all
 */

import com.example.restaurantTableReservation.Admin_Management.dto.SupportMessageAdminView;
import com.example.restaurantTableReservation.User_Management.model.SupportContactMessage;
import com.example.restaurantTableReservation.User_Management.service.SupportContactMessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class AdminSupportMessageApiController {

    private final SupportContactMessageService supportContactMessageService;

    public AdminSupportMessageApiController(SupportContactMessageService supportContactMessageService) {
        this.supportContactMessageService = supportContactMessageService;
    }

    @GetMapping(value = "/admin/api/support-messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list(HttpSession session) {
        if (session == null || session.getAttribute("loggedAdmin") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false));
        }
        List<SupportMessageAdminView> out = supportContactMessageService.findAllNewestFirst().stream()
                .map(this::toView)
                .toList();
        return ResponseEntity.ok(out);
    }

    @PostMapping(value = "/admin/api/support-messages/{id}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id, HttpSession session) {
        if (session == null || session.getAttribute("loggedAdmin") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false));
        }
        try {
            supportContactMessageService.markAdminRead(id);
            return ResponseEntity.ok(Map.of("ok", true, "unreadCount", supportContactMessageService.countUnread()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("ok", false, "message", e.getMessage()));
        }
    }

    @PostMapping(value = "/admin/api/support-messages/read-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> markAllRead(HttpSession session) {
        if (session == null || session.getAttribute("loggedAdmin") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false));
        }
        supportContactMessageService.markAllAdminRead();
        return ResponseEntity.ok(Map.of("ok", true, "unreadCount", 0L));
    }

    @DeleteMapping(value = "/admin/api/support-messages/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteOne(@PathVariable Long id, HttpSession session) {
        if (session == null || session.getAttribute("loggedAdmin") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false));
        }
        try {
            supportContactMessageService.deleteMessage(id);
            return ResponseEntity.ok(Map.of("ok", true, "unreadCount", supportContactMessageService.countUnread()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("ok", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping(value = "/admin/api/support-messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteAll(HttpSession session) {
        if (session == null || session.getAttribute("loggedAdmin") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false));
        }
        supportContactMessageService.deleteAllMessages();
        return ResponseEntity.ok(Map.of("ok", true, "unreadCount", 0L));
    }

    private SupportMessageAdminView toView(SupportContactMessage m) {
        SupportMessageAdminView v = new SupportMessageAdminView();
        v.setId(m.getId());
        v.setCategory(m.getCategory());
        v.setEmail(m.getEmail());
        v.setDescription(m.getDescription());
        v.setMessageSource(m.getMessageSource());
        v.setAttemptedLoginEmail(m.getAttemptedLoginEmail());
        v.setRestaurantId(m.getRestaurantId());
        v.setRestaurantName(m.getRestaurantName());
        v.setOwnerUserId(m.getOwnerUserId());
        v.setAdminRead(m.isAdminRead());
        v.setCreatedAt(m.getCreatedAt());
        return v;
    }
}
