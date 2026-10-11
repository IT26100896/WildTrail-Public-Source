package com.wildtrail;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.bookings.JdbcBookingRepository;
import com.wildtrail.modules.payments.JdbcPaymentRepository;
import com.wildtrail.modules.payments.PaymentService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FailedPaymentDeletionTest {
    @Test void deletionChecksPaymentAgainAfterLockingBooking() {
        JdbcBookingRepository bookings = mock(JdbcBookingRepository.class);
        JdbcPaymentRepository payments = mock(JdbcPaymentRepository.class);
        when(payments.payment(7)).thenReturn(Map.of("bookingId", 3, "status", "FAILED"), Map.of("bookingId", 3, "status", "PAID"));
        when(bookings.lockBooking(3)).thenReturn(Map.of("id", 3));
        PaymentService service = new PaymentService(bookings, payments);
        assertThrows(ApiException.class, () -> service.deleteFailedPayment(7));
        verify(bookings).lockBooking(3);
        verify(payments, never()).deleteFailedPayment(7);
    }
}
