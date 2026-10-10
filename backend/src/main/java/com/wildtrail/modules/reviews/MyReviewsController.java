package com.wildtrail.modules.reviews;
import com.wildtrail.exception.ApiException;
import com.wildtrail.security.SessionUser;
import com.wildtrail.util.RequestValues;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/reviews/mine") @PreAuthorize("hasRole('CUSTOMER')")
public class MyReviewsController {
    private final JdbcTemplate jdbc;
    public MyReviewsController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping public List<Map<String,Object>> all(Authentication auth){return jdbc.queryForList("SELECT r.id,r.rating,r.comment,r.approved,r.created_at AS \"createdAt\" FROM reviews r JOIN customers c ON c.id=r.customer_id WHERE c.user_id=? ORDER BY r.created_at DESC",uid(auth));}
    @PutMapping("/{id}")public Map<String,Boolean> update(@PathVariable int id,@RequestBody Map<String,Object> body,Authentication auth){
        Review review=new Review(RequestValues.integer(body,"rating",1,5),RequestValues.text(body,"comment",1000));
        if(jdbc.update("UPDATE reviews SET rating=?,comment=?,approved=FALSE WHERE id=? AND customer_id IN (SELECT id FROM customers WHERE user_id=?)",review.getRating(),review.getComment(),id,uid(auth))==0)throw ApiException.notFound("Review was not found");return Map.of("ok",true);
    }
    @DeleteMapping("/{id}")public Map<String,Boolean> remove(@PathVariable int id,Authentication auth){if(jdbc.update("DELETE FROM reviews WHERE id=? AND customer_id IN (SELECT id FROM customers WHERE user_id=?)",id,uid(auth))==0)throw ApiException.notFound("Review was not found");return Map.of("ok",true);}
    private int uid(Authentication auth){return ((SessionUser)auth.getPrincipal()).uid();}
}
