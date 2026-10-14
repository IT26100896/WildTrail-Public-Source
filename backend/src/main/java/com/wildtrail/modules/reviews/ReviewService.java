package com.wildtrail.modules.reviews;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.users.JdbcCustomerRepository;
import com.wildtrail.security.SessionUser;
import com.wildtrail.util.RequestValues;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final JdbcReviewRepository reviews;
    private final JdbcCustomerRepository customers;

    public ReviewService(JdbcReviewRepository reviews, JdbcCustomerRepository customers) {
        this.reviews = reviews;
        this.customers = customers;
    }

    public List<Map<String, Object>> reviews(boolean publicView) { return reviews.reviews(publicView); }
    public List<Map<String, Object>> mine(int userId) { return reviews.mine(userId); }

    @Transactional
    public Map<String, Object> submitReview(Map<String, Object> body, SessionUser user) {
        if (user == null) throw ApiException.unauthorized("Please sign in to write a review");
        String country = RequestValues.optionalText(body, "country");
        if (country.length() > 80) throw ApiException.badRequest("Country is too long");
        int rating = RequestValues.integer(body, "rating", 1, 5);
        String comment = RequestValues.text(body, "comment", 1000);
        Map<String, Object> customer = customers.ensureForUser(user);
        int customerId = ((Number) customer.get("id")).intValue();
        Review review = customers.hasCompletedBooking(customerId)
                ? new VerifiedReview(rating, comment) : new Review(rating, comment);
        // Account identity is authoritative, even if the request contains another name or email.
        return reviews.createReview(customerId, user, country, review.getRating(), review.getComment(), review.getType());
    }

    public void deleteReview(int id) {
        if (!reviews.deleteReview(id)) throw ApiException.notFound("Review was not found");
    }

    public void deleteMine(int id, int userId) {
        if (!reviews.deleteMine(id, userId)) throw ApiException.notFound("Review was not found");
    }

    @Transactional
    public Map<String, Object> reply(int id, Map<String, Object> body, SessionUser user) {
        if (!reviews.reviewExists(id)) throw ApiException.notFound("Review was not found");
        return reviews.reply(id, user, RequestValues.text(body, "comment", 1000));
    }

    public void deleteReply(int id, int replyId) {
        if (!reviews.deleteReply(id, replyId)) throw ApiException.notFound("Reply was not found");
    }
}
