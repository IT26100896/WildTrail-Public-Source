package com.wildtrail;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.reviews.JdbcReviewRepository;
import com.wildtrail.modules.reviews.ReviewService;
import com.wildtrail.modules.users.JdbcCustomerRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReviewCountryTest {
    @Test void overlyLongCountryIsRejectedBeforeDatabaseInsert() {
        JdbcReviewRepository reviews = mock(JdbcReviewRepository.class);
        JdbcCustomerRepository customers = mock(JdbcCustomerRepository.class);
        when(customers.customerByUser(5)).thenReturn(Map.of("id", 2, "fullName", "Reviewer"));
        ReviewService service = new ReviewService(reviews, customers);
        var details = Map.<String, Object>of("country", "A".repeat(81), "rating", 5, "comment", "Excellent safari");
        assertThrows(ApiException.class, () -> service.submitReview(details, 5));
        verify(reviews, never()).createReview(any(), anyString(), anyString(), anyInt(), anyString(), anyString());
    }
}
