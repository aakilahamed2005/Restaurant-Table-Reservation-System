package com.example.restaurantTableReservation.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class UploadExceptionHandler {

    private static final String UPLOAD_ERROR_MESSAGE =
            "Upload too large. Each image must be 20MB or less, and the total upload must be 200MB or less.";

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(MaxUploadSizeExceededException ex,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", UPLOAD_ERROR_MESSAGE);

        String uri = request.getRequestURI();
        if (uri.contains("/restaurant-edit/")) {
            String id = uri.substring(uri.lastIndexOf('/') + 1);
            return "redirect:/restaurant-edit/" + id;
        }
        if (uri.contains("/restaurant-register")) {
            return "redirect:/restaurant-register";
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            int pathStart = referer.indexOf('/', referer.indexOf("://") + 3);
            if (pathStart > 0) {
                return "redirect:" + referer.substring(pathStart);
            }
        }

        return "redirect:/owner-restaurants";
    }
}
