package com.wildtrail.modules.resources;

import java.util.List;

public class SafariJeep
extends Vehicle {
    public SafariJeep(int id, String name, int capacity, boolean fourByFour, List<String> features, boolean available, double price, String imageUrl) {
        super(id, name, capacity, fourByFour, features, available, price, imageUrl);
    }

    @Override
    public String getCategory() {
        return "STANDARD";
    }

    @Override
    public String getCategoryLabel() {
        return "Safari Jeep";
    }

    @Override
    protected List<String> standardFeatures() {
        List<String> result = super.standardFeatures();
        result.add("Open-top viewing");
        return result;
    }
}
