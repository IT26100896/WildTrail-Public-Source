package com.wildtrail.modules.payments;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.bookings.Booking;
import com.wildtrail.modules.bookings.JdbcBookingRepository;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private static final List<String> PAYMENT_STATUSES=List.of("PENDING","PAID","REFUNDED","FAILED");
    private final JdbcBookingRepository bookings;
    private final com.wildtrail.modules.payments.JdbcPaymentRepository payments;
    public PaymentService(JdbcBookingRepository bookings,com.wildtrail.modules.payments.JdbcPaymentRepository payments){this.bookings=bookings;this.payments=payments;}
    public List<Map<String, Object>> payments() {
        return this.payments.payments();
    }
    @Transactional
    public void updatePaymentStatus(int id, String status) {
        if (!PAYMENT_STATUSES.contains(status)) {
            throw ApiException.badRequest("Invalid payment status");
        }
        Map<String,Object> payment=this.payments.payment(id);
        if (payment == null) {
            throw ApiException.notFound("Payment with id " + id + " was not found");
        }
        int bookingId=((Number)payment.get("bookingId")).intValue();
        var booking=bookings.lockBooking(bookingId);
        if(booking==null)throw ApiException.notFound("Booking was not found");
        payment=payments.payment(id);
        if(payment==null)throw ApiException.notFound("Payment was not found");
        String old=payment.get("status").toString();
        if (status.equals(old)) return;
        boolean paid=old.equals("PENDING") && status.equals("PAID") && !"CANCELLED".equals(booking.get("status"));
        boolean refund=old.equals("PAID") && status.equals("REFUNDED");
        if(!paid && !refund) throw ApiException.conflict("This payment status transition is not allowed");
        this.payments.updatePaymentStatus(id, status);
        if(paid && "PENDING".equals(booking.get("status"))) bookings.updateBookingStatus(bookingId,"CONFIRMED");
        if(refund && !"COMPLETED".equals(booking.get("status"))) bookings.updateBookingStatus(bookingId,"CANCELLED");
    }
    @Transactional
    public void deleteFailedPayment(int id) {
        var payment=payments.payment(id); if(payment==null) throw ApiException.notFound("Payment was not found");
        int bookingId=((Number)payment.get("bookingId")).intValue();
        if(bookings.lockBooking(bookingId)==null)throw ApiException.notFound("Booking was not found");
        payment=payments.payment(id); if(payment==null)throw ApiException.notFound("Payment was not found");
        if(!"FAILED".equals(payment.get("status"))) throw ApiException.conflict("Only failed payment records may be removed");
        payments.deleteFailedPayment(id);
    }
}
