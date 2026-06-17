package com.example.restaurantTableReservation.ChatBot.controller;


import com.example.restaurantTableReservation.ChatBot.model.ChatMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Controller
public class ChatController {

    @Value("${github.models.token}")
    private String token;

    @Value("${github.models.base-url}")
    private String baseUrl;

    @Value("${github.models.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/chat")
    public String chatPage() {
        return "chat-bot";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        String userMessage = (String) body.get("message");

        try {
            // Verify token is configured and has valid format
            // Accept both classic (ghp_) and fine-grained (github_pat_) token formats
            if (token == null || token.isBlank()) {
                System.err.println("⚠️ INVALID TOKEN: Token is missing or empty in application.properties");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("reply", "❌ Chatbot is not properly configured. The API token is missing. Please contact the administrator to update github.models.token in application.properties"));
            }

            if (!token.startsWith("ghp_") && !token.startsWith("github_pat_")) {
                System.err.println("⚠️ INVALID TOKEN FORMAT: Token must start with 'ghp_' (classic) or 'github_pat_' (fine-grained)");
                System.err.println("Token preview: " + token.substring(0, Math.min(20, token.length())) + "...");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("reply", "❌ Chatbot is not properly configured. The API token format is invalid.\n\nExpected format:\n• Classic token: ghp_...\n• Fine-grained token: github_pat_...\n\nPlease check application.properties"));
            }

            // Get or create history from session
            @SuppressWarnings("unchecked")
            List<ChatMessage> history = (List<ChatMessage>) session.getAttribute("chatHistory");
            if (history == null) history = new ArrayList<>();

            // Build messages for API
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system",
                    "content", "You are a helpful support assistant for DineElite restaurant reservation system. Answer questions about restaurants, reservations, and dining experiences. Answer clearly and concisely."));
            for (ChatMessage msg : history) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            // Build request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            System.out.println("🔄 Sending chat request to: " + baseUrl + "/chat/completions");
            System.out.println("📦 Model: " + model);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions",
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            System.out.println("✅ API Response Status: " + response.getStatusCode());

            // Extract reply safely
            Map<String, Object> body_data = response.getBody();
            if (body_data == null || !body_data.containsKey("choices")) {
                return ResponseEntity.ok(Map.of("reply", "I received an empty response from the API. Please try again."));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) body_data.get("choices");
            if (choices == null || choices.isEmpty()) {
                return ResponseEntity.ok(Map.of("reply", "No response generated. Please try again."));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
            String reply = (String) messageObj.get("content");

            // Update session history
            history.add(new ChatMessage("user", userMessage));
            history.add(new ChatMessage("assistant", reply));
            session.setAttribute("chatHistory", history);

            return ResponseEntity.ok(Map.of("reply", reply));

        } catch (HttpClientErrorException.Unauthorized e) {
            System.err.println("❌ API AUTHENTICATION FAILED (401)");
            System.err.println("   Reason: Invalid, expired, or revoked token");
            System.err.println("   Token used: " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "NULL"));
            System.err.println("   Response: " + e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("reply", "❌ Authentication failed! The API token is invalid or expired.\n\n🔧 To fix this:\n1. Go to https://github.com/settings/tokens\n2. Create a new GitHub token with repo scope\n3. Update github.models.token in application.properties\n4. Restart the application"));
        } catch (HttpClientErrorException e) {
            System.err.println("❌ API ERROR: " + e.getStatusCode());
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Response: " + e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("reply", "⚠️ API Error (" + e.getStatusCode().value() + "): " + e.getMessage() + "\n\nPlease check the server logs for details."));
        } catch (Exception e) {
            System.err.println("❌ UNEXPECTED ERROR: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("reply", "❌ Unexpected error: " + e.getMessage() + "\n\nPlease check the server logs."));
        }
    }

    @PostMapping("/api/chat/clear")
    @ResponseBody
    public ResponseEntity<Void> clearChat(HttpSession session) {
        session.removeAttribute("chatHistory");
        return ResponseEntity.ok().build();
    }

    /**
     * Diagnostic endpoint to check token configuration and API connectivity
     */
    @GetMapping("/api/chat/diagnostic")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> diagnostic() {
        Map<String, Object> diagnostics = new HashMap<>();

        // Check token
        diagnostics.put("token_configured", token != null && !token.isBlank());
        if (token != null && !token.isBlank()) {
            boolean isClassicFormat = token.startsWith("ghp_");
            boolean isFineGrainedFormat = token.startsWith("github_pat_");
            boolean isValidFormat = isClassicFormat || isFineGrainedFormat;

            diagnostics.put("token_format_valid", isValidFormat);
            diagnostics.put("token_type", isClassicFormat ? "Classic (ghp_)" : isFineGrainedFormat ? "Fine-grained (github_pat_)" : "INVALID");
            diagnostics.put("token_preview", token.length() > 30 ? token.substring(0, 30) + "..." : "SHORT_TOKEN");
            diagnostics.put("token_length", token.length());

            if (!isValidFormat) {
                diagnostics.put("error", "❌ INVALID TOKEN FORMAT - Expected: ghp_... or github_pat_...");
            }
        } else {
            diagnostics.put("error", "❌ TOKEN NOT CONFIGURED - Check application.properties for github.models.token");
        }

        // Check configuration
        diagnostics.put("base_url", baseUrl);
        diagnostics.put("model", model);
        diagnostics.put("api_endpoint", baseUrl + "/chat/completions");

        // Try API connection
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);

            Map<String, Object> testRequest = Map.of(
                    "model", model,
                    "messages", List.of(Map.of("role", "user", "content", "test")),
                    "max_tokens", 10
            );

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions",
                    new HttpEntity<>(testRequest, headers),
                    Map.class
            );

            diagnostics.put("api_status", "✅ CONNECTED");
            diagnostics.put("api_response_code", response.getStatusCode().value());
        } catch (HttpClientErrorException.Unauthorized e) {
            diagnostics.put("api_status", "❌ AUTHENTICATION FAILED (401)");
            diagnostics.put("issue", "Token is invalid, expired, or revoked");
            diagnostics.put("solution", "1. Generate new token at https://github.com/settings/tokens\n2. The token must start with 'ghp_' (classic) or 'github_pat_' (fine-grained)\n3. Update github.models.token in application.properties\n4. Restart the application");
        } catch (HttpClientErrorException e) {
            diagnostics.put("api_status", "❌ API ERROR");
            diagnostics.put("error_code", e.getStatusCode().value());
            diagnostics.put("error_message", e.getMessage());
        } catch (Exception e) {
            diagnostics.put("api_status", "❌ CONNECTION ERROR");
            diagnostics.put("error_message", e.getMessage());
        }

        return ResponseEntity.ok(diagnostics);
    }
}