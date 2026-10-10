package com.wildtrail.modules.users;

import com.wildtrail.exception.ApiException;
import com.wildtrail.security.SessionCookieService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/auth"})
public class AuthController {
    private final AuthService authService;
    private final SessionCookieService sessions;

    public AuthController(AuthService authService, SessionCookieService sessions) {
        this.authService = authService;
        this.sessions = sessions;
    }

    @PostMapping(value={"/{action}"})
    public ResponseEntity<Map<String, Object>> action(@PathVariable String action, @RequestBody(required=false) Map<String, Object> requestBody, HttpServletResponse response) {
        Map<String, Object> body = requestBody == null ? Map.of() : requestBody;
        return switch (action) {
            case "login" -> ResponseEntity.ok(this.begin(this.authService.login(body, null), response));
            case "admin-login" -> ResponseEntity.ok(this.begin(this.authService.login(body, "ROLE_ADMIN"), response));
            case "register" -> ResponseEntity.status((int)201).body(this.begin(this.authService.register(body), response));
            case "logout" -> this.logoutAll(response);
            case "admin-logout" -> this.logout(response, "ROLE_ADMIN");
            default -> throw ApiException.notFound("Auth action " + action + " was not found");
        };
    }

    private Map<String, Object> begin(User user, HttpServletResponse response) {
        this.sessions.start(response, user);
        return Map.of("ok", true, "name", user.getFullName(), "redirect", user.getHomePath());
    }

    private ResponseEntity<Map<String, Object>> logout(HttpServletResponse response, String role) {
        this.sessions.end(response, role);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private ResponseEntity<Map<String, Object>> logoutAll(HttpServletResponse response) {
        this.sessions.end(response, "ROLE_ADMIN");
        this.sessions.end(response, "ROLE_CUSTOMER");
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
