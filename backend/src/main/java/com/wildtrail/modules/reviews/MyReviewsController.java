package com.wildtrail.modules.reviews;

import com.wildtrail.security.SessionUser;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews/mine")
@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
public class MyReviewsController {
    private final ReviewService reviews;
    public MyReviewsController(ReviewService reviews) { this.reviews = reviews; }

    @GetMapping
    public List<Map<String, Object>> mine(Authentication auth) {
        return reviews.mine(((SessionUser) auth.getPrincipal()).uid());
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable int id, Authentication auth) {
        reviews.deleteMine(id, ((SessionUser) auth.getPrincipal()).uid());
        return Map.of("ok", true);
    }
}
