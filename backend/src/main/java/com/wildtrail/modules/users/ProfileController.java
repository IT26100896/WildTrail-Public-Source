package com.wildtrail.modules.users;

import com.wildtrail.exception.ApiException;
import com.wildtrail.security.SessionCookieService;
import com.wildtrail.security.SessionUser;
import com.wildtrail.util.RequestValues;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
public class ProfileController {
    private final JdbcTemplate jdbc;
    private final SessionCookieService cookies;
    private final JdbcCustomerRepository customers;
    public ProfileController(JdbcTemplate jdbc, SessionCookieService cookies, JdbcCustomerRepository customers) {
        this.jdbc = jdbc;
        this.cookies = cookies;
        this.customers = customers;
    }

    @GetMapping
    public Map<String, Object> get(Authentication auth) {
        // A new admin does not need a customer row just to view their profile.
        return jdbc.queryForMap("""
                SELECT u.id,u.full_name AS "fullName",u.email,u.role,c.phone,c.country
                FROM users u LEFT JOIN customers c ON c.user_id=u.id WHERE u.id=?
                """, uid(auth));
    }

    @PutMapping
    @Transactional
    public Map<String, Object> update(@RequestBody Map<String, Object> body, Authentication auth) {
        int id = uid(auth);
        SessionUser user = (SessionUser) auth.getPrincipal();
        String name = RequestValues.text(body, "fullName", 120);
        String email = RequestValues.email(body);
        String phone = RequestValues.optionalText(body, "phone");
        String country = RequestValues.optionalText(body, "country");
        if (phone.length() > 40 || country.length() > 80) throw ApiException.badRequest("Contact details are too long");
        String newPassword = RequestValues.optionalText(body, "newPassword");
        // Changing login email or password requires the account's current password.
        if (!newPassword.isBlank() || !email.equalsIgnoreCase(user.email())) {
            String stored = jdbc.queryForObject("SELECT password_hash FROM users WHERE id=?", String.class, id);
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(RequestValues.password(body), stored)) throw ApiException.unauthorized("Current password is incorrect");
            if (!newPassword.isBlank()) {
                String next = RequestValues.password(Map.of("password", newPassword));
                if (next.length() < 8) throw ApiException.badRequest("New password must be at least 8 characters");
                jdbc.update("UPDATE users SET password_hash=? WHERE id=?", encoder.encode(next), id);
            }
        }
        customers.ensureForUser(user);
        jdbc.update("UPDATE users SET full_name=?,email=? WHERE id=?", name, email, id);
        jdbc.update("UPDATE customers SET full_name=?,email=?,phone=?,country=? WHERE user_id=?", name, email, phone, country, id);
        return get(auth);
    }

    @DeleteMapping
    @Transactional
    @PreAuthorize("hasRole('CUSTOMER')")
    public Map<String, Boolean> remove(Authentication auth, HttpServletResponse response) {
        int id = uid(auth);
        if (jdbc.queryForObject("SELECT COUNT(*) FROM bookings b JOIN customers c ON c.id=b.customer_id WHERE c.user_id=?", Integer.class, id) > 0) {
            throw ApiException.conflict("Accounts with booking records must be retained. Contact staff.");
        }
        jdbc.update("DELETE FROM customers WHERE user_id=?", id);
        jdbc.update("DELETE FROM users WHERE id=?", id);
        cookies.end(response, "ROLE_CUSTOMER");
        return Map.of("ok", true);
    }

    private int uid(Authentication auth) { return ((SessionUser) auth.getPrincipal()).uid(); }
}
