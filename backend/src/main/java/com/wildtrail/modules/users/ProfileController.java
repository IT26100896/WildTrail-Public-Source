package com.wildtrail.modules.users;
import com.wildtrail.exception.ApiException;
import com.wildtrail.security.*;
import com.wildtrail.util.RequestValues;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/profile") @PreAuthorize("hasRole('CUSTOMER')")
public class ProfileController {
    private final JdbcTemplate jdbc;
    private final SessionCookieService cookies;
    public ProfileController(JdbcTemplate jdbc,SessionCookieService cookies){this.jdbc=jdbc;this.cookies=cookies;}
    @GetMapping public Map<String,Object> get(Authentication auth){return jdbc.queryForMap("SELECT u.id,u.full_name AS \"fullName\",u.email,c.phone,c.country FROM users u JOIN customers c ON c.user_id=u.id WHERE u.id=?",uid(auth));}
    @PutMapping @Transactional public Map<String,Object> update(@RequestBody Map<String,Object> body,Authentication auth){
        int id=uid(auth);
        String name=RequestValues.text(body,"fullName",120),email=RequestValues.email(body),phone=RequestValues.optionalText(body,"phone"),country=RequestValues.optionalText(body,"country");
        if(phone.length()>40 || country.length()>80)throw ApiException.badRequest("Contact details are too long");
        if(!RequestValues.optionalText(body,"newPassword").isBlank()){
            String stored=jdbc.queryForObject("SELECT password_hash FROM users WHERE id=?",String.class,id);
            if(!new BCryptPasswordEncoder().matches(RequestValues.password(body),stored))throw ApiException.unauthorized("Current password is incorrect");
            String next=RequestValues.password(Map.of("password",body.get("newPassword")));
            if(next.length()<8)throw ApiException.badRequest("New password must be at least 8 characters");
            jdbc.update("UPDATE users SET password_hash=? WHERE id=?",new BCryptPasswordEncoder().encode(next),id);
        }
        jdbc.update("UPDATE users SET full_name=?,email=? WHERE id=?",name,email,id);
        jdbc.update("UPDATE customers SET full_name=?,email=?,phone=?,country=? WHERE user_id=?",name,email,phone,country,id);
        return get(auth);
    }
    @DeleteMapping @Transactional public Map<String,Boolean> remove(Authentication auth,HttpServletResponse response){
        int id=uid(auth);
        if(jdbc.queryForObject("SELECT COUNT(*) FROM bookings b JOIN customers c ON c.id=b.customer_id WHERE c.user_id=?",Integer.class,id)>0)throw ApiException.conflict("Accounts with booking records must be retained. Contact staff.");
        jdbc.update("DELETE FROM customers WHERE user_id=?",id);jdbc.update("DELETE FROM users WHERE id=?",id);cookies.end(response,"ROLE_CUSTOMER");return Map.of("ok",true);
    }
    private int uid(Authentication auth){return ((SessionUser)auth.getPrincipal()).uid();}
}
