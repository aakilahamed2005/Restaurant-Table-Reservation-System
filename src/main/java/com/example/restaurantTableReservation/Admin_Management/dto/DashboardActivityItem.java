package com.example.restaurantTableReservation.Admin_Management.dto; // Package: data objects for admin UI (not always DB tables)

/*
 * One row in the admin dashboard "Recent activity" widget.
 * Built in AdminService.getRecentActivities().
 */

public class DashboardActivityItem {

    private final String message; // HTML text, e.g. "Customer <strong>Jane</strong> reviewed ..."
    private final String timeLabel; // Relative time, e.g. "5 minutes ago" or "Awaiting review"
    private final String dotColor; // CSS color for the small circle, e.g. "var(--terracotta)"

    // Constructor sets all three fields (immutable after creation)
    public DashboardActivityItem(String message, String timeLabel, String dotColor) {
        this.message = message;
        this.timeLabel = timeLabel;
        this.dotColor = dotColor;
    }

    public String getMessage() { // Thymeleaf reads this in admin-dashboard.html
        return message;
    }

    public String getTimeLabel() { // Shown on the right of each activity row
        return timeLabel;
    }

    public String getDotColor() { // Inline style or class for activity dot
        return dotColor;
    }
}
