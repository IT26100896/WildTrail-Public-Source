package com.wildtrail.modules.users;

import com.wildtrail.exception.ApiException;
import com.wildtrail.util.RequestValues;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import org.bouncycastle.crypto.generators.SCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JdbcUserRepository users;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(JdbcUserRepository users) {
        this.users = users;
    }

    public User login(Map<String, Object> body, String requiredRole) {
        String email = RequestValues.email(body);
        String password = RequestValues.password(body);
        Map<String, Object> row = this.users.findByEmail(email);
        if (row == null || !this.verifyAndMigrate(password, (String)row.get("passwordHash"), ((Number)row.get("id")).intValue())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        String role = (String)row.get("role");
        if (requiredRole != null && !requiredRole.equals(role)) {
            throw ApiException.unauthorized("ROLE_ADMIN".equals(requiredRole) ? "This account does not have administrator access" : "Please use the admin portal to sign in");
        }
        return User.create(role, ((Number)row.get("id")).intValue(), (String)row.get("email"), (String)row.get("fullName"));
    }

    @org.springframework.transaction.annotation.Transactional
    public User register(Map<String, Object> body) {
        String name = RequestValues.text(body, "fullName", 120);
        String email = RequestValues.email(body);
        String password = RequestValues.password(body);
        if (password.length() < 8) {
            throw ApiException.badRequest("Password must be at least 8 characters");
        }
        if (this.users.findByEmail(email) != null) {
            throw ApiException.conflict("An account with this email already exists");
        }
        Map<String, Object> row = this.users.create(email, this.passwordEncoder.encode((CharSequence)password), name);
        this.users.linkGuestCustomer(((Number)row.get("id")).intValue(), email, name, RequestValues.optionalText(body, "phone"), RequestValues.optionalText(body, "country"));
        return User.create("ROLE_CUSTOMER", ((Number)row.get("id")).intValue(), email, name);
    }

    private boolean verifyAndMigrate(String password, String encoded, int userId) {
        if (encoded.startsWith("$2a$") || encoded.startsWith("$2b$") || encoded.startsWith("$2y$")) {
            return this.passwordEncoder.matches((CharSequence)password, encoded);
        }
        String[] parts = encoded.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] expected = HexFormat.of().parseHex(parts[1]);
            byte[] actual = SCrypt.generate((byte[])password.getBytes(StandardCharsets.UTF_8), (byte[])parts[0].getBytes(StandardCharsets.UTF_8), (int)16384, (int)8, (int)1, (int)64);
            if (!MessageDigest.isEqual(expected, actual)) {
                return false;
            }
            this.users.updatePasswordHash(userId, this.passwordEncoder.encode((CharSequence)password));
            return true;
        }
        catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
