package com.example.restaurantTableReservation.Restaurant_Mangement;

/**
 * Represents the lifecycle status of a restaurant record.
 *
 * - ACTIVE: visible and bookable by customers.
 * - SUSPENDED: temporarily disabled by admin (reservations are cancelled).
 * - DEACTIVATED: soft-deleted by owner; retained for history but not visible.
 * - PENDING: newly created and awaiting admin approval.
 */
public enum Status {
    // Restaurant is approved and available to customers
    ACTIVE,

    // Restaurant has been suspended by administrators (temporary)
    SUSPENDED,

    // Soft-deleted: owner removed the listing but record is kept for history
    DEACTIVATED,

    // Newly submitted by owner; waiting for review/approval
    PENDING
}
