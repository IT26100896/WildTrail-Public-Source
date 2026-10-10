package com.wildtrail.modules.users;

import java.util.Map;
import com.wildtrail.repository.JdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository {
    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> findByEmail(String email) {
        return this.jdbc.query("SELECT id, email, password_hash AS \"passwordHash\", full_name AS \"fullName\", role FROM users WHERE email=?", result -> result.next() ? Map.of("id", result.getInt("id"), "email", result.getString("email"), "passwordHash", result.getString("passwordHash"), "fullName", result.getString("fullName"), "role", result.getString("role")) : null, new Object[]{email.toLowerCase()});
    }

    public Map<String, Object> create(String email, String passwordHash, String fullName) {
        Integer id = JdbcInsert.id(this.jdbc, "INSERT INTO users(email, password_hash, full_name, role)\nVALUES (?, ?, ?, 'ROLE_CUSTOMER') RETURNING id\n", new Object[]{email.toLowerCase(), passwordHash, fullName});
        return this.findByEmail(email);
    }

    public void updatePasswordHash(int id, String passwordHash) {
        this.jdbc.update("UPDATE users SET password_hash=? WHERE id=?", new Object[]{passwordHash, id});
    }

    public void linkGuestCustomer(int userId, String email, String fullName, String phone, String country) {
        int updated = this.jdbc.update("UPDATE customers SET user_id=?, full_name=?, phone=COALESCE(NULLIF(?, ''), phone),\n    country=COALESCE(NULLIF(?, ''), country)\nWHERE email=? AND (user_id IS NULL OR user_id=?)\n", new Object[]{userId, fullName, phone, country, email.toLowerCase(), userId});
        if (updated == 0) {
            this.jdbc.update("INSERT INTO customers(user_id, full_name, email, phone, country)\nVALUES (?, ?, ?, NULLIF(?, ''), NULLIF(?, ''))\n", new Object[]{userId, fullName, email.toLowerCase(), phone, country});
        }
    }
}

