package com.wildtrail.modules.users;

import com.wildtrail.exception.ApiException;
import com.wildtrail.repository.JdbcInsert;
import com.wildtrail.security.SessionUser;
import com.wildtrail.util.RequestValues;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/** Only an authenticated administrator may provision another staff account. */
@RestController
@RequestMapping("/api/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {
    private final JdbcTemplate jdbc;
    private final AuthService auth;

    public AdminAccountController(JdbcTemplate jdbc, AuthService auth) {
        this.jdbc = jdbc;
        this.auth = auth;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return jdbc.queryForList("SELECT u.id,u.email,u.full_name AS \"fullName\",a.staff_title AS \"staffTitle\",u.created_at AS \"createdAt\" FROM users u JOIN admins a ON a.user_id=u.id WHERE u.role='ROLE_ADMIN' ORDER BY u.id");
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @Transactional
    public Map<String, Object> create(@RequestBody Map<String, Object> body, Authentication authentication) {
        SessionUser actor = (SessionUser) authentication.getPrincipal();
        // Re-entering the current operator's password confirms this privilege-granting action.
        auth.login(Map.of("email", actor.email(), "password", RequestValues.password(Map.of("password", body.getOrDefault("currentPassword", "")))), "ROLE_ADMIN");
        String email = RequestValues.email(body);
        String name = RequestValues.text(body, "fullName", 120);
        String title = RequestValues.text(body, "staffTitle", 80);
        String password = RequestValues.password(body);
        if (password.length() < 12) throw ApiException.badRequest("Administrator password must contain at least 12 characters");
        if (jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email=?", Integer.class, email) > 0)
            throw ApiException.conflict("An account with this email already exists");
        int id = JdbcInsert.id(jdbc, "INSERT INTO users(email,password_hash,full_name,role) VALUES (?,?,?,'ROLE_ADMIN')", email, new BCryptPasswordEncoder().encode(password), name);
        jdbc.update("INSERT INTO admins(user_id,staff_title) VALUES (?,?)", id, title);
        return Map.of("id", id, "email", email, "fullName", name, "staffTitle", title);
    }
}
