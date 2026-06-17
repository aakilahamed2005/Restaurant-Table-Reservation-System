package com.example.restaurantTableReservation.Admin_Management.util; // Package: small helper utilities for admin UI

/*
 * Used on list pages (users, restaurants, reservations, feedback).
 * Decides whether a search/filter query parameter should actually filter the list.
 */

public final class AdminFilterHelper { // final = cannot be subclassed

    private AdminFilterHelper() { // Private constructor: utility class, no instances
    }

    /**
     * @param value query string from HTML form (may be null, blank, or "all")
     * @return true if we should apply this filter to the list
     */
    public static boolean hasFilterValue(String value) {
        if (value == null) { // No parameter sent from browser
            return false; // Do not filter
        }
        String trimmed = value.trim(); // Remove leading/trailing spaces
        // Empty string or dropdown option "All" means show everything
        return !trimmed.isEmpty() && !"all".equalsIgnoreCase(trimmed);
    }
}
