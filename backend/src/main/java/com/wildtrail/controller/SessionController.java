package com.wildtrail.controller;

import com.wildtrail.security.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
public class SessionController {
    private final SessionCookieService cookies;
    private final JdbcTemplate jdbc;
    public SessionController(SessionCookieService cookies, JdbcTemplate jdbc) { this.cookies=cookies; this.jdbc=jdbc; }
    @GetMapping("/api/auth/me")
    public Map<String, ?> me(@RequestParam(defaultValue="client") String area, HttpServletRequest request) {
        String role = area.equals("admin") ? "ROLE_ADMIN" : "ROLE_CUSTOMER";
        SessionUser user = cookies.read(request, role);
        if (user == null) return Map.of();
        var rows = jdbc.queryForList("SELECT id,email,full_name AS \"name\",role FROM users WHERE id=? AND role=?", user.uid(), role);
        if (rows.isEmpty()) return Map.of();
        var row = rows.get(0);
        return Map.of("uid",row.get("id"),"email",row.get("email"),"name",row.get("name"),"role",role,"exp",System.currentTimeMillis()+1000);
    }
}
