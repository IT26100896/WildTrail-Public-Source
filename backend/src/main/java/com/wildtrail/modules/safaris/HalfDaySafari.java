package com.wildtrail.modules.safaris;

public final class HalfDaySafari
extends SafariPackage {
    public HalfDaySafari(int id, String name, int destinationId, String destinationName, int durationHours, String startTime, int maxGuests, double basePrice, String description, String imageUrl, boolean active) {
        super(id, name, destinationId, destinationName, durationHours, startTime, maxGuests, basePrice, description, imageUrl, active);
    }

    @Override
    public String getType() {
        return "HALF_DAY";
    }

    @Override
    public String getTypeLabel() {
        return "Half Day Safari";
    }

    @Override
    public String getPricingNote() {
        return "per guest";
    }

    @Override
    public String[] getInclusions() {
        return new String[]{"Park entrance assistance", "Experienced tracker", "Bottled water", "Light refreshments", "Reservoir viewpoint stop"};
    }
}
