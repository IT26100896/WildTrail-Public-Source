package com.wildtrail.modules.resources;

import java.util.List;

public final class FamilySafariJeep
extends SafariJeep {
    public FamilySafariJeep(int id, String name, int capacity, boolean fourByFour, List<String> features, boolean available, double price, String imageUrl) {
        super(id, name, capacity, fourByFour, features, available, price, imageUrl);
    }

    @Override
    public String getCategory() {
        return "FAMILY";
    }

    @Override
    public String getCategoryLabel() {
        return "Family Jeep";
    }

    @Override
    protected List<String> standardFeatures() {
        List<String> result = super.standardFeatures();
        result.add("Kid-safe railings");
        return result;
    }
}
