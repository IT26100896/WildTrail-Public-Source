package com.wildtrail.modules.users;

public final class Admin
extends User {
    public Admin(int id, String email, String fullName) {
        super(id, email, fullName);
    }

    @Override
    public String getRole() {
        return "ROLE_ADMIN";
    }

    @Override
    public String getHomePath() {
        return "/admin/dashboard";
    }

    @Override
    public boolean canManageSystem() {
        return true;
    }
}
