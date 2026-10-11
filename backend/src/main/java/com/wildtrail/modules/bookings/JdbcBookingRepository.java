package com.wildtrail.modules.bookings;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import com.wildtrail.repository.JdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcBookingRepository {
    // Shared by the admin list, customer history and booking confirmation.
    private static final String DETAIL_SELECT = """
            SELECT b.id, b.reference, b.safari_date AS "safariDate", b.time_slot AS "timeSlot",
                   b.guests, b.total_amount AS "totalAmount", b.status,
                   b.special_requests AS "specialRequests", b.created_at AS "createdAt",
                   b.customer_id AS "customerId", c.full_name AS "customerName",
                   c.email AS "customerEmail", c.phone AS "customerPhone",
                   b.package_id AS "packageId", p.name AS "packageName",
                   d.name AS "destinationName", b.vehicle_id AS "vehicleId",
                   v.name AS "vehicleName", pay.method AS "paymentMethod",
                   pay.status AS "paymentStatus", pay.transaction_ref AS "transactionRef"
            FROM bookings b
            JOIN customers c ON c.id = b.customer_id
            JOIN safari_packages p ON p.id = b.package_id
            JOIN destinations d ON d.id = p.destination_id
            JOIN vehicles v ON v.id = b.vehicle_id
            LEFT JOIN payments pay ON pay.booking_id = b.id
            """;
    private final JdbcTemplate jdbc;

    public JdbcBookingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> allDetailed() {
        return this.jdbc.queryForList(DETAIL_SELECT + " ORDER BY b.created_at DESC");
    }

    public List<Map<String, Object>> forCustomer(int customerId) {
        return this.jdbc.queryForList(
                DETAIL_SELECT + " WHERE b.customer_id=? ORDER BY b.created_at DESC",
                customerId);
    }

    public List<Map<String, Object>> byReference(String reference) {
        return this.jdbc.queryForList(
                DETAIL_SELECT + " WHERE b.reference=? ORDER BY b.created_at DESC",
                reference);
    }

    public Map<String, Object> basic(int id) {
        String sql = """
                SELECT id, reference, customer_id AS "customerId", package_id AS "packageId",
                       vehicle_id AS "vehicleId", safari_date AS "safariDate",
                       time_slot AS "timeSlot", guests,
                       special_requests AS "specialRequests", total_amount AS "totalAmount", status
                FROM bookings WHERE id = ?
                """;
        List<Map<String, Object>> rows = this.jdbc.queryForList(sql, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int countVehicleBookings(int vehicleId, LocalDate date, String slot) {
        String sql = """
                SELECT COUNT(*) FROM bookings
                WHERE vehicle_id = ? AND safari_date = ? AND time_slot = ?
                      AND status <> 'CANCELLED'
                """;
        Integer count = this.jdbc.queryForObject(sql, Integer.class, vehicleId, date, slot);
        return count == null ? 0 : count;
    }

    public List<Integer> availableVehicleIds(LocalDate date, String slot) {
        String sql = """
                SELECT v.id FROM vehicles v
                WHERE v.available = TRUE
                  AND NOT EXISTS (
                      SELECT 1 FROM bookings b
                      WHERE b.vehicle_id = v.id AND b.safari_date = ?
                        AND b.time_slot = ? AND b.status <> 'CANCELLED'
                  )
                ORDER BY v.id
                """;
        return this.jdbc.queryForList(sql, Integer.class, date, slot);
    }

    public int create(Map<String, Object> values) {
        String sql = """
                INSERT INTO bookings(reference, customer_id, package_id, vehicle_id,
                                     safari_date, time_slot, guests, special_requests,
                                     total_amount, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, NULLIF(?, ''), ?, ?) RETURNING id
                """;
        return JdbcInsert.id(this.jdbc, sql, new Object[]{
                values.get("reference"), values.get("customerId"), values.get("packageId"),
                values.get("vehicleId"), values.get("safariDate"), values.get("timeSlot"),
                values.get("guests"), values.get("specialRequests"), values.get("totalAmount"),
                values.get("status")});
    }

    // Locking rows prevents simultaneous requests from taking the same resource.
    public void lockGuideSchedule() {
        jdbc.queryForObject("SELECT id FROM guide_schedule_lock WHERE id=1 FOR UPDATE", Integer.class);
    }

    public void lockVehicle(int id) {
        jdbc.queryForObject("SELECT id FROM vehicles WHERE id=? FOR UPDATE", Integer.class, id);
    }

    public Map<String, Object> lockBooking(int id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id FROM bookings WHERE id=? FOR UPDATE", id);
        return rows.isEmpty() ? null : basic(id);
    }

    public boolean overlaps(int vehicleId, LocalDate date, String slot, int durationHours, int excludedId) {
        int start = LocalTime.parse(slot).toSecondOfDay();
        int end = start + durationHours * 3600;
        String sql = """
                SELECT b.time_slot AS "slot", p.duration_hours AS "hours"
                FROM bookings b
                JOIN safari_packages p ON p.id = b.package_id
                WHERE b.vehicle_id = ? AND b.safari_date = ?
                  AND b.status <> 'CANCELLED' AND b.id <> ?
                """;
        List<Map<String, Object>> rows = jdbc.queryForList(
                sql, vehicleId, date, excludedId);
        // Two departures overlap when each starts before the other finishes.
        return rows.stream().anyMatch(row -> {
            int otherStart = LocalTime.parse(row.get("slot").toString()).toSecondOfDay();
            int otherEnd = otherStart + ((Number) row.get("hours")).intValue() * 3600;
            return start < otherEnd && otherStart < end;
        });
    }

    public void edit(int id, LocalDate date, String slot, int guests, String requests, double total) {
        String guideConflictSql = """
                SELECT COUNT(*) FROM booking_guide_assignments mine
                JOIN booking_guide_assignments other ON other.guide_id = mine.guide_id
                JOIN bookings b ON b.id = other.booking_id
                WHERE mine.booking_id = ? AND b.id <> ? AND b.safari_date = ?
                  AND b.status <> 'CANCELLED'
                """;
        Integer guideConflicts = jdbc.queryForObject(
                guideConflictSql, Integer.class, id, id, date);
        if (guideConflicts != null && guideConflicts > 0) {
            throw com.wildtrail.exception.ApiException.conflict(
                    "Assigned guide is unavailable on the requested date. Contact staff.");
        }
        jdbc.update("""
                UPDATE bookings
                SET safari_date = ?, time_slot = ?, guests = ?, special_requests = ?,
                    total_amount = ?, updated_at = NOW()
                WHERE id = ?
                """, date, slot, guests, requests, total, id);
        jdbc.update("""
                UPDATE payments SET amount = ?
                WHERE booking_id = ? AND status = 'PENDING'
                """, total, id);
    }

    public void updateBookingStatus(int id, String status) {
        this.jdbc.update("UPDATE bookings SET status=?, updated_at=NOW() WHERE id=?", new Object[]{status, id});
    }

    public boolean delete(int id) {
        return this.jdbc.update("DELETE FROM bookings WHERE id=?", new Object[]{id}) > 0;
    }
}

