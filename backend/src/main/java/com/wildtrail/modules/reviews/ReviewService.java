package com.wildtrail.modules.reviews;

import com.wildtrail.exception.ApiException;
import com.wildtrail.util.RequestValues;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final JdbcReviewRepository people;
    private final com.wildtrail.modules.users.JdbcCustomerRepository customers;

    public ReviewService(JdbcReviewRepository people, com.wildtrail.modules.users.JdbcCustomerRepository customers) {
        this.people = people;
        this.customers=customers;
    }

    public List<Map<String, Object>> reviews(boolean approvedOnly) {
        return this.people.reviews(approvedOnly);
    }

    public Map<String, Object> submitReview(Map<String, Object> body, Integer userId) {
        Map<String, Object> customer = userId == null ? null : this.customers.customerByUser(userId);
        if(customer==null)throw ApiException.unauthorized("Please sign in to write a review");
        String author=customer.get("fullName").toString();
        String country = RequestValues.optionalText(body, "country");
        int rating = RequestValues.integer(body, "rating", 1, 5);
        String comment = RequestValues.text(body, "comment", 1000);
        com.wildtrail.modules.reviews.Review review=customers.hasCompletedBooking(((Number)customer.get("id")).intValue()) ? new com.wildtrail.modules.reviews.VerifiedReview(rating,comment) : new com.wildtrail.modules.reviews.Review(rating,comment);
        rating=review.getRating();comment=review.getComment();
        return this.people.createReview(Integer.valueOf(((Number)customer.get("id")).intValue()), author, country, rating, comment, review.getType());
    }

    public Map<String, Object> approveReview(int id, Map<String, Object> body) {
        boolean approved = RequestValues.bool(body, "approved", false);
        Map<String, Object> review = this.people.setReviewApproved(id, approved);
        if (review == null) {
            throw ApiException.notFound("Review with id " + id + " was not found");
        }
        return review;
    }

    public void deleteReview(int id) {
        if (!this.people.deleteReview(id)) {
            throw ApiException.notFound("Review with id " + id + " was not found");
        }
    }

}
