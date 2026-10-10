package com.wildtrail.modules.users;

public abstract class User {
    private final int id;
    private final String email;
    private final String fullName;

    protected User(int id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }

    public int getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getFullName() {
        return this.fullName;
    }

    public abstract String getRole();

    public abstract String getHomePath();

    public abstract boolean canManageSystem();

    public static User create(String role, int id, String email, String fullName) {
        return "ROLE_ADMIN".equals(role) ? new Admin(id, email, fullName) : new Customer(id, email, fullName);
    }
}
