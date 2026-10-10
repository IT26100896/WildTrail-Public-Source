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
    public Map<String,Boolean> contact(@RequestBody Map<String,Object> body){
        String phone=RequestValues.optionalText(body,"phone");if(phone.length()>40)throw ApiException.badRequest("Phone number is too long");
        jdbc.update("INSERT INTO contact_messages(name,email,phone,message)VALUES (?,?,?,?)",RequestValues.text(body,"name",120),RequestValues.email(body),phone,RequestValues.text(body,"message",2000));return Map.of("ok",true);
    }
}
