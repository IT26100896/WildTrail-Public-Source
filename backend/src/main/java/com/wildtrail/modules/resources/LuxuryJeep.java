package com.wildtrail.modules.resources;

import java.util.List;

public final class LuxuryJeep
extends Vehicle {
    public LuxuryJeep(int id, String name, int capacity, boolean fourByFour, List<String> features, boolean available, double price, String imageUrl) {
        super(id, name, capacity, fourByFour, features, available, price, imageUrl);
    }

    @Override
    public String getCategory() {
        return "LUXURY";
    }

    @Override
    public String getCategoryLabel() {
        return "Luxury Jeep";
    }

    @Override
    protected List<String> standardFeatures() {
        List<String> result = super.standardFeatures();
        result.add("Complimentary refreshments");
        result.add("Personal concierge");
        return result;
    }

    @Override
    public double calculateCost(int durationHours) {
        return super.calculateCost(durationHours) + 20.0;
    }
}
