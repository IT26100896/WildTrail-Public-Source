package com.wildtrail.modules.bookings;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.safaris.SafariPackage;
import com.wildtrail.modules.resources.Vehicle;
import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;

public final class Booking {
    private static final Map<String, Set<String>> TRANSITIONS = Map.of("PENDING", Set.of("CONFIRMED", "CANCELLED"), "CONFIRMED", Set.of("COMPLETED", "CANCELLED"), "COMPLETED", Set.of(), "CANCELLED", Set.of());
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
        if (!safariPackage.isBookable()) throw ApiException.badRequest("This package is unavailable");
        vehicle.ensureBookableFor(guests);
        double packageCost = safariPackage.calculatePrice(guests);
        double vehicleCost = vehicle.calculateCost(safariPackage.getDurationHours());
        return Map.of("packageCost", packageCost, "vehicleCost", vehicleCost, "total", SafariPackage.round(packageCost + vehicleCost));
    }

    public static java.util.List<String> slots(SafariPackage item) {
        java.util.Set<String> slots = new java.util.TreeSet<>();
        slots.add(item.getStartTime());
        if (java.util.Set.of("EVENING", "FAMILY").contains(item.getType())) slots.addAll(java.util.List.of("14:00", "14:30"));
        else if (item.getType().equals("PRIVATE")) slots.addAll(java.util.List.of("05:30", "06:00", "14:00", "14:30"));
        else slots.addAll(java.util.List.of("05:30", "06:00"));
        return slots.stream().filter(s -> java.time.LocalTime.parse(s).toSecondOfDay() + item.getDurationHours()*3600 <= 86400).toList();
    }

    public static void validateSlot(SafariPackage item, String slot) {
        if (!slots(item).contains(slot)) throw ApiException.badRequest("Departure time is not offered by this package");
    }

    public static String validateDate(String rawDate) {
        try {
            LocalDate date = LocalDate.parse(rawDate);
            if (!date.isAfter(LocalDate.now(java.time.ZoneId.of("Asia/Colombo")))) {
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
        if (departure.isBefore(now.plus(java.time.Duration.ofHours(24)))) {
            throw ApiException.badRequest("Changes require at least 24 hours notice");
        }
    }
}
