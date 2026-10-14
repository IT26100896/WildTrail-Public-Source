package com.wildtrail.controller;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @PreAuthorize("hasRole('ADMIN')")
public class InboxController {
    private final JdbcTemplate jdbc;
    public InboxController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping("/api/admin/messages")public List<Map<String,Object>> all(){return jdbc.queryForList("SELECT id,name,email,phone,message,sender_role AS \"senderRole\",created_at AS \"createdAt\" FROM contact_messages ORDER BY created_at DESC");}
}
