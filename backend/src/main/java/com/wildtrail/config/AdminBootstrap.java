package com.wildtrail.config;

import com.wildtrail.repository.JdbcInsert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Local operator provisioning only; public registration always creates customers. */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final String email, password;
    public AdminBootstrap(JdbcTemplate jdbc, @Value("${ADMIN_EMAIL:}") String email, @Value("${ADMIN_PASSWORD:}") String password) {
        this.jdbc = jdbc; this.email = email; this.password = password;
    }
    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE role='ROLE_ADMIN'", Integer.class) > 0) return;
        if (email.isBlank() || password.isBlank()) throw new IllegalStateException("No administrator configured. Run scripts/setup.ps1 first.");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password.length() < 12 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Admin email and a 12-72 byte password are required");
        int id = JdbcInsert.id(jdbc, "INSERT INTO users(email,password_hash,full_name,role) VALUES (?,?,?,'ROLE_ADMIN')", email.toLowerCase(), new BCryptPasswordEncoder().encode(password), "Safari Administrator");
        jdbc.update("INSERT INTO admins(user_id) VALUES (?)", id);
    }
}
