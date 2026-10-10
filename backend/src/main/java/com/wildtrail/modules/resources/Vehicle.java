package com.wildtrail.modules.resources;

import com.wildtrail.exception.ApiException;
import java.util.ArrayList;
import java.util.List;

public abstract class Vehicle {
    private final int id;
    private final String name;
    private final int capacity;
    private final boolean fourByFour;
    private final List<String> features;
    private final boolean available;
    private final double price;
    private final String imageUrl;

    protected Vehicle(int id, String name, int capacity, boolean fourByFour, List<String> features, boolean available, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.fourByFour = fourByFour;
        this.features = List.copyOf(features);
        this.available = available;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean isFourByFour() {
        return this.fourByFour;
    }

    public List<String> getFeatures() {
        return this.features;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public double getPrice() {
        return this.price;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public abstract String getCategory();

    public abstract String getCategoryLabel();

    protected List<String> standardFeatures() {
        return this.fourByFour ? new ArrayList<String>(List.of("4x4 drive")) : new ArrayList();
    }

    public List<String> getAllFeatures() {
        List<String> all = this.standardFeatures();
        for (String feature : this.features) {
            if (all.contains(feature)) continue;
            all.add(feature);
        }
        return all;
    }

    public double calculateCost(int durationHours) {
        return this.price * (double)(durationHours > 6 ? 2 : 1);
    }

    public void ensureBookableFor(int guests) {
        if (!this.available) {
            throw ApiException.badRequest(this.name + " is not available");
        }
        if (guests > this.capacity) {
            throw ApiException.badRequest(this.name + " seats a maximum of " + this.capacity + " guests");
        }
    }
}
