package com.wildtrail.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wildtrail.modules.users.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SessionCookieService {
    public static final String ADMIN_COOKIE = "wt_admin_session";
    public static final String CLIENT_COOKIE = "wt_client_session";
    private static final long MAX_AGE_SECONDS = 28800L;
    private final ObjectMapper mapper;
    private final String secret;
    private final boolean secureCookies;

    public SessionCookieService(ObjectMapper mapper, @Value(value="${SESSION_SECRET}") String secret, @Value(value="${SECURE_COOKIES:false}") boolean secureCookies) {
        this.mapper = mapper;
        if (secret.length() < 32) throw new IllegalArgumentException("SESSION_SECRET must contain at least 32 characters");
        this.secret = secret;
        this.secureCookies = secureCookies;
    }

    public void start(HttpServletResponse response, User user) {
        try {
            long expiresAt = System.currentTimeMillis() + 28800000L;
            String json = this.mapper.writeValueAsString(Map.of("uid", user.getId(), "email", user.getEmail(), "name", user.getFullName(), "role", user.getRole(), "exp", expiresAt));
            String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String token = payload + "." + this.sign(payload);
            response.addHeader("Set-Cookie", ResponseCookie.from((String)this.cookieName(user.getRole()), (String)token).httpOnly(true).secure(this.secureCookies).sameSite("Lax").path("/").maxAge(Duration.ofSeconds(28800L)).build().toString());
        }
        catch (Exception exception) {
            throw new IllegalStateException("Unable to create a login session", exception);
        }
    }

    public void end(HttpServletResponse response, String role) {
        response.addHeader("Set-Cookie", ResponseCookie.from((String)this.cookieName(role), (String)"").httpOnly(true).secure(this.secureCookies).sameSite("Lax").path("/").maxAge(Duration.ZERO).build().toString());
    }

    public SessionUser read(HttpServletRequest request, String role) {
        String name = this.cookieName(role);
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (!name.equals(cookie.getName())) continue;
            try {
                String[] parts = cookie.getValue().split("\\.", 2);
                if (parts.length != 2 || !MessageDigest.isEqual(this.sign(parts[0]).getBytes(StandardCharsets.US_ASCII), parts[1].getBytes(StandardCharsets.US_ASCII))) {
                    return null;
                }
                byte[] json = Base64.getUrlDecoder().decode(parts[0]);
                Map claims = (Map)this.mapper.readValue(json, Map.class);
                if (!role.equals(claims.get("role"))) {
                    return null;
                }
                long expires = ((Number)claims.get("exp")).longValue();
                if (expires <= System.currentTimeMillis()) {
                    return null;
                }
                return new SessionUser(((Number)claims.get("uid")).intValue(), (String)claims.get("email"), (String)claims.get("name"), role);
            }
            catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String cookieName(String role) {
        return "ROLE_ADMIN".equals(role) ? ADMIN_COOKIE : CLIENT_COOKIE;
    }

    private String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.US_ASCII)));
    }
}
