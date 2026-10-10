package com.wildtrail.modules.users;

public final class Customer
extends User {
    public Customer(int id, String email, String fullName) {
        super(id, email, fullName);
    }

    @Override
    public String getRole() {
        return "ROLE_CUSTOMER";
    }

    @Override
    public String getHomePath() {
        return "/client/my-bookings";
    }

    @Override
    public boolean canManageSystem() {
        return false;
    }
}
