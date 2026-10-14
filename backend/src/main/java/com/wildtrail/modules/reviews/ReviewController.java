package com.wildtrail.modules.reviews;

import com.wildtrail.security.SessionUser;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviews;
    public ReviewController(ReviewService reviews) { this.reviews = reviews; }

    @GetMapping
    public List<Map<String, Object>> reviews(@RequestParam(defaultValue="0") String all, Authentication auth) {
        boolean admin = auth != null && auth.getAuthorities().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getAuthority()));
        return reviews.reviews(!admin || !"1".equals(all));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, Object> body, Authentication auth) {
        Map<String, Object> review = reviews.submitReview(body, (SessionUser) auth.getPrincipal());
        return ResponseEntity.status(201).body(Map.of("ok", true, "id", review.get("id"),
                "message", "Thank you! Your review is now published."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Boolean> delete(@PathVariable int id) {
        reviews.deleteReview(id);
        return Map.of("ok", true);
    }

    @PostMapping("/{id}/replies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reply(@PathVariable int id,
            @RequestBody Map<String, Object> body, Authentication auth) {
        return ResponseEntity.status(201).body(reviews.reply(id, body, (SessionUser) auth.getPrincipal()));
    }

    @DeleteMapping("/{id}/replies/{replyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Boolean> deleteReply(@PathVariable int id, @PathVariable int replyId) {
        reviews.deleteReply(id, replyId);
        return Map.of("ok", true);
    }
}
