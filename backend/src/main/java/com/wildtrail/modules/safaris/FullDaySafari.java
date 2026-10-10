package com.wildtrail.modules.safaris;

public final class FullDaySafari
extends SafariPackage {
    public FullDaySafari(int id, String name, int destinationId, String destinationName, int durationHours, String startTime, int maxGuests, double basePrice, String description, String imageUrl, boolean active) {
        super(id, name, destinationId, destinationName, durationHours, startTime, maxGuests, basePrice, description, imageUrl, active);
    }

    @Override
    public String getType() {
        return "FULL_DAY";
    }

    @Override
    public String getTypeLabel() {
        return "Full Day Safari";
    }

    @Override
    public String getPricingNote() {
        return "per guest \u00b7 10% off for 4+";
    }

    @Override
    public String[] getInclusions() {
        return new String[]{"Park entrance assistance", "Experienced tracker", "Bottled water", "Breakfast & picnic lunch", "Deep-block access", "Beach stop"};
    }

    @Override
    public double calculatePrice(int guests) {
        double subtotal = super.calculatePrice(guests);
        return guests >= 4 ? FullDaySafari.round(subtotal * 0.9) : subtotal;
    }
}
