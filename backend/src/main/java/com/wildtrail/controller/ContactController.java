package com.wildtrail.controller;
import com.wildtrail.util.RequestValues;
import com.wildtrail.exception.ApiException;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
@RestController
public class ContactController {
    private final JdbcTemplate jdbc;
    public ContactController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @PostMapping("/api/contact") @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Map<String,Boolean> contact(@RequestBody Map<String,Object> body,org.springframework.security.core.Authentication authentication){
        String phone=RequestValues.optionalText(body,"phone");if(phone.length()>40)throw ApiException.badRequest("Phone number is too long");
        com.wildtrail.security.SessionUser user = authentication != null && authentication.getPrincipal() instanceof com.wildtrail.security.SessionUser account ? account : null;
        String name = user == null ? RequestValues.text(body,"name",120) : user.name();
        String email = user == null ? RequestValues.email(body) : user.email();
        jdbc.update("INSERT INTO contact_messages(name,email,phone,message,sender_role)VALUES (?,?,?,?,?)",name,email,phone,RequestValues.text(body,"message",2000),user == null ? null : user.role());return Map.of("ok",true);
    }
}
