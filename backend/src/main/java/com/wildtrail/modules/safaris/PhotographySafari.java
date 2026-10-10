package com.wildtrail.modules.safaris;

public final class PhotographySafari
extends SafariPackage {
    public PhotographySafari(int id, String name, int destinationId, String destinationName, int durationHours, String startTime, int maxGuests, double basePrice, String description, String imageUrl, boolean active) {
        super(id, name, destinationId, destinationName, durationHours, startTime, maxGuests, basePrice, description, imageUrl, active);
    }

    @Override
    public String getType() {
        return "PHOTOGRAPHY";
    }

    @Override
    public String getTypeLabel() {
        return "Photography Safari";
    }

    @Override
    public String getPricingNote() {
        return "per guest + $25 gear fee";
    }

    @Override
    public String[] getInclusions() {
        return new String[]{"Park entrance assistance", "Experienced tracker", "Bottled water", "Photographer guide", "Bean bags & mounts", "Golden-hour access"};
    }

    @Override
    public double calculatePrice(int guests) {
        return PhotographySafari.round(super.calculatePrice(guests) + 25.0);
    }
}
