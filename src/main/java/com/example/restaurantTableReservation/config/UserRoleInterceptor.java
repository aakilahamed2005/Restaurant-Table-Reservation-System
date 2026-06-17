package com.example.restaurantTableReservation.config;

import com.example.restaurantTableReservation.User_Management.Role;
import com.example.restaurantTableReservation.User_Management.Status;
import com.example.restaurantTableReservation.User_Management.model.UserModel;
import com.example.restaurantTableReservation.User_Management.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Ensures owner-only URLs are only used when the session user is an OWNER,
 * and customer-only URLs only when the session user is a CUSTOMER.
 * Redirects deactivated accounts to the home page on navigation.
 */
@Component
public class UserRoleInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public UserRoleInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String context = request.getContextPath();
        String uri = request.getRequestURI();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }

        if (uri.startsWith("/admin")
                || uri.startsWith("/uploads/")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/webjars/")
                || uri.startsWith("/error")) {
            return true;
        }

        if (isAuthOrPublic(uri)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        UserModel user = session == null ? null : (UserModel) session.getAttribute("loggedInUser");

        boolean ownerPath = isOwnerRestrictedPath(uri);
        boolean customerPath = isCustomerRestrictedPath(uri);

        if (!ownerPath && !customerPath) {
            return true;
        }

        if (user == null) {
            if (ownerPath) {
                response.sendRedirect(context + "/sign-in?tab=partner");
            } else {
                response.sendRedirect(context + "/sign-in?tab=customer");
            }
            return false;
        }

        UserModel freshUser = userRepository.findById(user.getId()).orElse(null);
        if (freshUser == null || freshUser.getStatus() == Status.DEACTIVATED) {
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(context + "/");
            return false;
        }

        session.setAttribute("loggedInUser", freshUser);
        user = freshUser;

        if (ownerPath && user.getRole() != Role.OWNER) {
            response.sendRedirect(context + "/restaurant-listing");
            return false;
        }

        if (customerPath && user.getRole() != Role.CUSTOMER) {
            response.sendRedirect(context + "/owner-restaurants");
            return false;
        }

        return true;
    }

    private boolean isAuthOrPublic(String uri) {
        return uri.equals("/")
                || uri.equals("/sign-in")
                || uri.equals("/customer-login")
                || uri.equals("/owner-login")
                || uri.equals("/user-logout")
                || uri.equals("/customer-register")
                || uri.equals("/owner-register")
                || uri.startsWith("/restaurant-listing")
                || uri.startsWith("/restaurant-view-page/")
                || uri.equals("/favicon.ico")
                || uri.equals("/support/contact-message");
    }

    private boolean isOwnerRestrictedPath(String uri) {
        return uri.startsWith("/owner-")
                || uri.startsWith("/owner/")
                || uri.equals("/restaurant-register")
                || uri.startsWith("/restaurant-edit/")
                || uri.startsWith("/restaurant-remove/");
    }

    private boolean isCustomerRestrictedPath(String uri) {
        return uri.startsWith("/customer-")
                || uri.startsWith("/customer/")
                || uri.equals("/reservation-save")
                || uri.startsWith("/restaurant-seat-checking/")
                || uri.equals("/restaurant-seat-availability")
                || uri.startsWith("/restaurant-feedback")
                || uri.startsWith("/delete-feedback/");
    }
}
