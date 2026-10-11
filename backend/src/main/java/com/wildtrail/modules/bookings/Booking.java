package com.wildtrail.modules.bookings;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.safaris.SafariPackage;
import com.wildtrail.modules.resources.Vehicle;
import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class Booking {
    // A completed or cancelled booking cannot move to another state.
    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "PENDING", Set.of("CONFIRMED", "CANCELLED"),
            "CONFIRMED", Set.of("COMPLETED", "CANCELLED"),
            "COMPLETED", Set.of(),
            "CANCELLED", Set.of());
    private String status;

    public Booking(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void transitionTo(String next) {
        if (next.equals(this.status)) {
            return;
        }
        if (!TRANSITIONS.getOrDefault(this.status, Set.of()).contains(next)) {
            throw ApiException.badRequest("Cannot change booking from " + this.status + " to " + next);
        }
        this.status = next;
    }

    public static Map<String, Double> quote(SafariPackage safariPackage, Vehicle vehicle, int guests) {
        if (!safariPackage.isBookable()) {
            throw ApiException.badRequest("This package is unavailable");
        }
        vehicle.ensureBookableFor(guests);
        double packageCost = safariPackage.calculatePrice(guests);
        double vehicleCost = vehicle.calculateCost(safariPackage.getDurationHours());
        return Map.of(
                "packageCost", packageCost,
                "vehicleCost", vehicleCost,
                "total", SafariPackage.round(packageCost + vehicleCost));
    }

    public static List<String> slots(SafariPackage item) {
        Set<String> slots = new TreeSet<>();
        slots.add(item.getStartTime());
        if (Set.of("EVENING", "FAMILY").contains(item.getType())) {
            slots.addAll(List.of("14:00", "14:30"));
        } else if (item.getType().equals("PRIVATE")) {
            slots.addAll(List.of("05:30", "06:00", "14:00", "14:30"));
        } else {
            slots.addAll(List.of("05:30", "06:00"));
        }
        // Do not offer a departure that would finish after midnight.
        return slots.stream()
                .filter(slot -> LocalTime.parse(slot).toSecondOfDay()
                        + item.getDurationHours() * 3600 <= 86400)
                .toList();
    }

    public static void validateSlot(SafariPackage item, String slot) {
        if (!slots(item).contains(slot)) {
            throw ApiException.badRequest("Departure time is not offered by this package");
        }
    }

    public static String validateDate(String rawDate) {
        try {
            LocalDate date = LocalDate.parse(rawDate);
            if (!date.isAfter(LocalDate.now(ZoneId.of("Asia/Colombo")))) {
                throw ApiException.badRequest("Please choose a future safari date");
            }
            return date.toString();
        }
        catch (DateTimeParseException exception) {
            throw ApiException.badRequest("Invalid safari date");
        }
    }

    public static void requireChangeNotice(LocalDate date, String slot, Instant now) {
        Instant departure = date.atTime(LocalTime.parse(slot)).atZone(ZoneId.of("Asia/Colombo")).toInstant();
        // Check the exact departure time in Sri Lanka, not just the calendar date.
        if (departure.isBefore(now.plus(java.time.Duration.ofHours(24)))) {
            throw ApiException.badRequest("Changes require at least 24 hours notice");
        }
    }
}
