package com.wildtrail.modules.reviews;

import java.util.List;
import java.util.Map;
import com.wildtrail.repository.JdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcReviewRepository {
    private final JdbcTemplate jdbc;

    public JdbcReviewRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> reviews(boolean approvedOnly) {
        String where = approvedOnly ? " WHERE approved=TRUE" : "";
        return this.jdbc.queryForList("SELECT id, customer_id AS \"customerId\", author_name AS \"authorName\", country,\n       rating, comment, approved, review_type AS \"reviewType\", created_at AS \"createdAt\"\nFROM reviews\n" + where + " ORDER BY created_at DESC");
    }

    public Map<String, Object> createReview(Integer customerId, String author, String country, int rating, String comment, String reviewType) {
        Integer id = JdbcInsert.id(this.jdbc, "INSERT INTO reviews(customer_id, author_name, country, rating, comment, approved, review_type)\nVALUES (?, ?, NULLIF(?, ''), ?, ?, FALSE, ?) RETURNING id\n", new Object[]{customerId, author, country, rating, comment, reviewType});
        return this.review(id);
    }

    public Map<String, Object> setReviewApproved(int id, boolean approved) {
        if (this.jdbc.update("UPDATE reviews SET approved=? WHERE id=?", new Object[]{approved, id}) == 0) {
            return null;
        }
        return this.review(id);
    }

    public boolean deleteReview(int id) {
        return this.jdbc.update("DELETE FROM reviews WHERE id=?", new Object[]{id}) > 0;
    }

    public boolean reviewExists(int id) {
        Integer count = (Integer)this.jdbc.queryForObject("SELECT COUNT(*) FROM reviews WHERE id=?", Integer.class, new Object[]{id});
        return count != null && count > 0;
    }

    private Map<String, Object> review(int id) {
        return this.jdbc.queryForMap("SELECT id, customer_id AS \"customerId\", author_name AS \"authorName\", country,\n       rating, comment, approved, review_type AS \"reviewType\", created_at AS \"createdAt\"\nFROM reviews WHERE id=?\n", new Object[]{id});
    }
}

