package com.wildtrail.modules.reviews;

import com.wildtrail.security.SessionUser;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api"})
public class ReviewController {
    private final ReviewService people;

    public ReviewController(ReviewService people) {
        this.people = people;
    }

    @GetMapping(value={"/reviews"})
    public List<Map<String, Object>> reviews(@RequestParam(defaultValue="0") String all, Authentication authentication) {
        boolean admin = authentication != null && authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return this.people.reviews(!"1".equals(all) || !admin);
    }

    @PostMapping(value={"/reviews"})
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> submitReview(@RequestBody Map<String, Object> body, Authentication authentication) {
        SessionUser user = this.principal(authentication);
        this.people.submitReview(body, user == null ? null : Integer.valueOf(user.uid()));
        return ResponseEntity.status((int)201).body(Map.of("ok", true, "message", "Thank you! Your review will appear after moderation."));
    }

    @PatchMapping(value={"/reviews/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Object> approveReview(@PathVariable int id, @RequestBody Map<String, Object> body) {
        return this.people.approveReview(id, body);
    }

    @DeleteMapping(value={"/reviews/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Boolean> deleteReview(@PathVariable int id) {
        this.people.deleteReview(id);
        return Map.of("ok", true);
    }

    private SessionUser principal(Authentication authentication) {
        Object object;
        if (authentication != null && (object = authentication.getPrincipal()) instanceof SessionUser) {
            SessionUser user = (SessionUser)object;
            return user;
        }
        return null;
    }
}
