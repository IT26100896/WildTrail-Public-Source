package com.wildtrail.modules.safaris;

import com.wildtrail.exception.ApiException;

public abstract class SafariPackage {
    private final int id;
    private final String name;
    private final int destinationId;
    private final String destinationName;
    private final int durationHours;
    private final String startTime;
    private final int maxGuests;
    private final double basePrice;
    private final String description;
    private final String imageUrl;
    private final boolean active;

    protected SafariPackage(int id, String name, int destinationId, String destinationName, int durationHours, String startTime, int maxGuests, double basePrice, String description, String imageUrl, boolean active) {
        this.id = id;
        this.name = name;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.durationHours = durationHours;
        this.startTime = startTime;
        this.maxGuests = maxGuests;
        this.basePrice = basePrice;
        this.description = description;
        this.imageUrl = imageUrl;
        this.active = active;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getDestinationId() {
        return this.destinationId;
    }

    public String getDestinationName() {
        return this.destinationName;
    }

    public int getDurationHours() {
        return this.durationHours;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public int getMaxGuests() {
        return this.maxGuests;
    }

    public double getBasePrice() {
        return this.basePrice;
    }

    public String getDescription() {
        return this.description;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public boolean isActive() {
        return this.active;
    }

    public abstract String getType();

    public abstract String getTypeLabel();

    public abstract String getPricingNote();

    public abstract String[] getInclusions();

    public double calculatePrice(int guests) {
        this.validateGuests(guests);
        return SafariPackage.round(this.basePrice * (double)guests);
    }

    public void validateGuests(int guests) {
        if (guests < 1) {
            throw ApiException.badRequest("At least one guest is required");
        }
        if (guests > this.maxGuests) {
            throw ApiException.badRequest(this.name + " allows a maximum of " + this.maxGuests + " guests");
        }
    }

    public boolean isBookable() {
        return this.active;
    }

    public static double round(double amount) {
        return java.math.BigDecimal.valueOf(amount).setScale(2,java.math.RoundingMode.HALF_UP).doubleValue();
    }
}
