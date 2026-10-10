package com.wildtrail.modules.safaris;

public final class PrivateSafari
extends SafariPackage {
    public PrivateSafari(int id, String name, int destinationId, String destinationName, int durationHours, String startTime, int maxGuests, double basePrice, String description, String imageUrl, boolean active) {
        super(id, name, destinationId, destinationName, durationHours, startTime, maxGuests, basePrice, description, imageUrl, active);
    }

    @Override
    public String getType() {
        return "PRIVATE";
    }

    @Override
    public String getTypeLabel() {
        return "Private Safari";
    }

    @Override
    public String getPricingNote() {
        return "flat price per party";
    }

    @Override
    public String[] getInclusions() {
        return new String[]{"Park entrance assistance", "Experienced tracker", "Bottled water", "Exclusive jeep", "Senior naturalist", "Flexible timing"};
    }

    @Override
    public double calculatePrice(int guests) {
        this.validateGuests(guests);
        return this.getBasePrice();
    }
}
