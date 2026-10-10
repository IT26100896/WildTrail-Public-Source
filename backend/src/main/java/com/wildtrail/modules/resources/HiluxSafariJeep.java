package com.wildtrail.modules.resources;

import java.util.List;

public final class HiluxSafariJeep
extends SafariJeep {
    public HiluxSafariJeep(int id, String name, int capacity, boolean fourByFour, List<String> features, boolean available, double price, String imageUrl) {
        super(id, name, capacity, fourByFour, features, available, price, imageUrl);
    }

    @Override
    public String getCategory() {
        return "HILUX";
    }

    @Override
    public String getCategoryLabel() {
        return "Toyota Hilux";
    }

    @Override
    protected List<String> standardFeatures() {
        List<String> result = super.standardFeatures();
        result.add("Smooth suspension");
        return result;
    }
}
