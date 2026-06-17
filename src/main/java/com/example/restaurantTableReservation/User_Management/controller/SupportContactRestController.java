package com.example.restaurantTableReservation.User_Management.controller;

import com.example.restaurantTableReservation.User_Management.dto.SupportContactRequestDto;
import com.example.restaurantTableReservation.User_Management.service.SupportContactMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SupportContactRestController {
    private final SupportContactMessageService supportContactMessageService;

    public SupportContactRestController(SupportContactMessageService supportContactMessageService) {
        this.supportContactMessageService = supportContactMessageService;
    }

    @PostMapping(value = "/support/contact-message", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> submitUserMessage(@RequestBody SupportContactRequestDto dto) {
        try {
            supportContactMessageService.saveUserAccountMessage(dto);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("ok", false, "message", e.getMessage()));
        }
    }
}
