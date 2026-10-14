package com.wildtrail.modules.reviews;

import com.wildtrail.repository.JdbcInsert;
import com.wildtrail.security.SessionUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcReviewRepository {
    private static final String SELECT = """
            SELECT id, customer_id AS "customerId", author_name AS "authorName",
                   author_user_id AS "authorUserId", author_email AS "authorEmail",
                   author_role AS "authorRole", country, rating, comment, approved,
                   is_sample AS "sample", review_type AS "reviewType", created_at AS "createdAt"
            FROM reviews
            """;
    private final JdbcTemplate jdbc;

    public JdbcReviewRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> reviews(boolean publicView) {
        return withReplies(jdbc.queryForList(SELECT + " ORDER BY created_at DESC"), publicView);
    }

    public List<Map<String, Object>> mine(int userId) {
        return withReplies(jdbc.queryForList(SELECT + " WHERE author_user_id=? ORDER BY created_at DESC", userId), false);
    }

    public Map<String, Object> createReview(int customerId, SessionUser user, String country,
                                          int rating, String comment, String type) {
        int id = JdbcInsert.id(jdbc, """
                INSERT INTO reviews(customer_id,author_user_id,author_name,author_email,author_role,
                                    country,rating,comment,approved,review_type)
                VALUES (?,?,?,?,?,NULLIF(?, ''),?,?,TRUE,?) RETURNING id
                """, customerId, user.uid(), user.name(), user.email(), user.role(), country, rating, comment, type);
        return jdbc.queryForMap(SELECT + " WHERE id=?", id);
    }

    public boolean deleteReview(int id) {
        return jdbc.update("DELETE FROM reviews WHERE id=?", id) > 0;
    }

    public boolean deleteMine(int id, int userId) {
        return jdbc.update("DELETE FROM reviews WHERE id=? AND author_user_id=?", id, userId) > 0;
    }

    public boolean reviewExists(int id) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM reviews WHERE id=?", Integer.class, id) > 0;
    }

    public Map<String, Object> reply(int id, SessionUser user, String comment) {
        int replyId = JdbcInsert.id(jdbc, """
                INSERT INTO review_replies(review_id,author_user_id,author_name,author_email,comment)
                VALUES (?,?,?,?,?) RETURNING id
                """, id, user.uid(), user.name(), user.email(), comment);
        return jdbc.queryForMap("""
                SELECT id,review_id AS "reviewId",author_name AS "authorName",
                       author_email AS "authorEmail",comment,created_at AS "createdAt"
                FROM review_replies WHERE id=?
                """, replyId);
    }

    public boolean deleteReply(int reviewId, int replyId) {
        return jdbc.update("DELETE FROM review_replies WHERE review_id=? AND id=?", reviewId, replyId) > 0;
    }

    private List<Map<String, Object>> withReplies(List<Map<String, Object>> reviews, boolean publicView) {
        // Fetch replies once rather than making a query for every review card.
        Map<Integer, List<Map<String, Object>>> grouped = new HashMap<>();
        List<Map<String, Object>> replies = jdbc.queryForList("""
                SELECT id,review_id AS "reviewId",author_name AS "authorName",
                       author_email AS "authorEmail",comment,created_at AS "createdAt"
                FROM review_replies ORDER BY created_at,id
                """);
        for (Map<String, Object> reply : replies) {
            if (publicView) reply.remove("authorEmail");
            int reviewId = ((Number) reply.get("reviewId")).intValue();
            grouped.computeIfAbsent(reviewId, key -> new ArrayList<>()).add(reply);
        }
        for (Map<String, Object> review : reviews) {
            if (publicView) {
                // Email addresses are available to staff and the author, never to anonymous visitors.
                review.remove("authorEmail");
                review.remove("authorUserId");
                review.remove("customerId");
            }
            int id = ((Number) review.get("id")).intValue();
            review.put("replies", grouped.getOrDefault(id, List.of()));
        }
        return reviews;
    }
}
