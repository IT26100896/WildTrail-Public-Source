package com.wildtrail.modules.payments;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPaymentRepository {
    private final JdbcTemplate jdbc;
    public JdbcPaymentRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
    public void createPayment(int bookingId, String method, double amount, String status, String transactionRef, String cardLast4, Instant paidAt) {
        this.jdbc.update("INSERT INTO payments(booking_id, method, amount, status, transaction_ref, card_last4, paid_at)\nVALUES (?, ?, ?, ?, ?, ?, ?)\n", new Object[]{bookingId, method, amount, status, transactionRef, cardLast4, paidAt == null ? null : Timestamp.from(paidAt)});
    }
    public Map<String, Object> paymentForBooking(int bookingId) {
        List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT id, booking_id AS \"bookingId\", method, amount, status,\n       transaction_ref AS \"transactionRef\", card_last4 AS \"cardLast4\", paid_at AS \"paidAt\"\nFROM payments WHERE booking_id=?\n", new Object[]{bookingId});
        return rows.isEmpty() ? null : rows.get(0);
    }
    public void updatePaymentStatus(int id, String status) {
        this.jdbc.update("UPDATE payments SET status=?, paid_at=CASE WHEN ?='PAID' THEN NOW() ELSE paid_at END WHERE id=?", new Object[]{status, status, id});
    }
    public List<Map<String, Object>> payments() {
        return this.jdbc.queryForList("SELECT p.id, p.booking_id AS \"bookingId\", p.method, p.amount, p.status,\n       p.transaction_ref AS \"transactionRef\", p.card_last4 AS \"cardLast4\",\n       p.paid_at AS \"paidAt\", p.created_at AS \"createdAt\",\n       b.reference AS \"bookingReference\", c.full_name AS \"customerName\", c.email AS \"customerEmail\", u.role AS \"customerRole\",\n       v.registration_number AS \"vehicleNumber\", b.pickup_place AS \"pickupPlace\"\nFROM payments p JOIN bookings b ON b.id=p.booking_id\nJOIN customers c ON c.id=b.customer_id\nLEFT JOIN users u ON u.id=c.user_id JOIN vehicles v ON v.id=b.vehicle_id\nORDER BY p.created_at DESC\n");
    }
    public Map<String, Object> payment(int id) {
        List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT id, booking_id AS \"bookingId\", method, amount, status,\n       transaction_ref AS \"transactionRef\", card_last4 AS \"cardLast4\", paid_at AS \"paidAt\"\nFROM payments WHERE id=?\n", new Object[]{id});
        return rows.isEmpty() ? null : rows.get(0);
    }
    public void deleteFailedPayment(int id) {
        jdbc.update("DELETE FROM payments WHERE id=? AND status='FAILED'", id);
    }
}

