package com.wildtrail.modules.bookings;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.wildtrail.repository.JdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcBookingRepository {
    private static final String DETAIL_SELECT = "SELECT b.id, b.reference, b.safari_date AS \"safariDate\", b.time_slot AS \"timeSlot\",\n       b.guests, b.total_amount AS \"totalAmount\", b.status, b.special_requests AS \"specialRequests\",\n       b.created_at AS \"createdAt\", b.customer_id AS \"customerId\", c.full_name AS \"customerName\",\n       c.email AS \"customerEmail\", c.phone AS \"customerPhone\", b.package_id AS \"packageId\",\n       p.name AS \"packageName\", d.name AS \"destinationName\", b.vehicle_id AS \"vehicleId\",\n       v.name AS \"vehicleName\", pay.method AS \"paymentMethod\", pay.status AS \"paymentStatus\",\n       pay.transaction_ref AS \"transactionRef\"\nFROM bookings b\nJOIN customers c ON c.id=b.customer_id\nJOIN safari_packages p ON p.id=b.package_id\nJOIN destinations d ON d.id=p.destination_id\nJOIN vehicles v ON v.id=b.vehicle_id\nLEFT JOIN payments pay ON pay.booking_id=b.id\n";
    private final JdbcTemplate jdbc;

    public JdbcBookingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> allDetailed() {
        return this.jdbc.queryForList("SELECT b.id, b.reference, b.safari_date AS \"safariDate\", b.time_slot AS \"timeSlot\",\n       b.guests, b.total_amount AS \"totalAmount\", b.status, b.special_requests AS \"specialRequests\",\n       b.created_at AS \"createdAt\", b.customer_id AS \"customerId\", c.full_name AS \"customerName\",\n       c.email AS \"customerEmail\", c.phone AS \"customerPhone\", b.package_id AS \"packageId\",\n       p.name AS \"packageName\", d.name AS \"destinationName\", b.vehicle_id AS \"vehicleId\",\n       v.name AS \"vehicleName\", pay.method AS \"paymentMethod\", pay.status AS \"paymentStatus\",\n       pay.transaction_ref AS \"transactionRef\"\nFROM bookings b\nJOIN customers c ON c.id=b.customer_id\nJOIN safari_packages p ON p.id=b.package_id\nJOIN destinations d ON d.id=p.destination_id\nJOIN vehicles v ON v.id=b.vehicle_id\nLEFT JOIN payments pay ON pay.booking_id=b.id\n ORDER BY b.created_at DESC");
    }

    public List<Map<String, Object>> forCustomer(int customerId) {
        return this.jdbc.queryForList("SELECT b.id, b.reference, b.safari_date AS \"safariDate\", b.time_slot AS \"timeSlot\",\n       b.guests, b.total_amount AS \"totalAmount\", b.status, b.special_requests AS \"specialRequests\",\n       b.created_at AS \"createdAt\", b.customer_id AS \"customerId\", c.full_name AS \"customerName\",\n       c.email AS \"customerEmail\", c.phone AS \"customerPhone\", b.package_id AS \"packageId\",\n       p.name AS \"packageName\", d.name AS \"destinationName\", b.vehicle_id AS \"vehicleId\",\n       v.name AS \"vehicleName\", pay.method AS \"paymentMethod\", pay.status AS \"paymentStatus\",\n       pay.transaction_ref AS \"transactionRef\"\nFROM bookings b\nJOIN customers c ON c.id=b.customer_id\nJOIN safari_packages p ON p.id=b.package_id\nJOIN destinations d ON d.id=p.destination_id\nJOIN vehicles v ON v.id=b.vehicle_id\nLEFT JOIN payments pay ON pay.booking_id=b.id\n WHERE b.customer_id=? ORDER BY b.created_at DESC", new Object[]{customerId});
    }

    public List<Map<String, Object>> byReference(String reference) {
        return this.jdbc.queryForList("SELECT b.id, b.reference, b.safari_date AS \"safariDate\", b.time_slot AS \"timeSlot\",\n       b.guests, b.total_amount AS \"totalAmount\", b.status, b.special_requests AS \"specialRequests\",\n       b.created_at AS \"createdAt\", b.customer_id AS \"customerId\", c.full_name AS \"customerName\",\n       c.email AS \"customerEmail\", c.phone AS \"customerPhone\", b.package_id AS \"packageId\",\n       p.name AS \"packageName\", d.name AS \"destinationName\", b.vehicle_id AS \"vehicleId\",\n       v.name AS \"vehicleName\", pay.method AS \"paymentMethod\", pay.status AS \"paymentStatus\",\n       pay.transaction_ref AS \"transactionRef\"\nFROM bookings b\nJOIN customers c ON c.id=b.customer_id\nJOIN safari_packages p ON p.id=b.package_id\nJOIN destinations d ON d.id=p.destination_id\nJOIN vehicles v ON v.id=b.vehicle_id\nLEFT JOIN payments pay ON pay.booking_id=b.id\n WHERE b.reference=? ORDER BY b.created_at DESC", new Object[]{reference});
    }

    public Map<String, Object> basic(int id) {
        List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT id, reference, customer_id AS \"customerId\", package_id AS \"packageId\",\n       vehicle_id AS \"vehicleId\", safari_date AS \"safariDate\", time_slot AS \"timeSlot\",\n       guests, special_requests AS \"specialRequests\", total_amount AS \"totalAmount\", status\nFROM bookings WHERE id=?\n", new Object[]{id});
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int countVehicleBookings(int vehicleId, LocalDate date, String slot) {
        Integer count = (Integer)this.jdbc.queryForObject("SELECT COUNT(*) FROM bookings\nWHERE vehicle_id=? AND safari_date=? AND time_slot=? AND status <> 'CANCELLED'\n", Integer.class, new Object[]{vehicleId, date, slot});
        return count == null ? 0 : count;
    }

    public List<Integer> availableVehicleIds(LocalDate date, String slot) {
        return this.jdbc.queryForList("SELECT v.id FROM vehicles v\nWHERE v.available=TRUE AND NOT EXISTS (\n    SELECT 1 FROM bookings b WHERE b.vehicle_id=v.id AND b.safari_date=? AND b.time_slot=?\n        AND b.status <> 'CANCELLED'\n)\nORDER BY v.id\n", Integer.class, new Object[]{date, slot});
    }

    public int create(Map<String, Object> values) {
        return JdbcInsert.id(this.jdbc, "INSERT INTO bookings(reference, customer_id, package_id, vehicle_id, safari_date, time_slot,\n                     guests, special_requests, total_amount, status)\nVALUES (?, ?, ?, ?, ?, ?, ?, NULLIF(?, ''), ?, ?) RETURNING id\n", new Object[]{values.get("reference"), values.get("customerId"), values.get("packageId"), values.get("vehicleId"), values.get("safariDate"), values.get("timeSlot"), values.get("guests"), values.get("specialRequests"), values.get("totalAmount"), values.get("status")});
    }

    public void lockGuideSchedule() {
        jdbc.queryForObject("SELECT id FROM guide_schedule_lock WHERE id=1 FOR UPDATE", Integer.class);
    }

    public void lockVehicle(int id) {
        jdbc.queryForObject("SELECT id FROM vehicles WHERE id=? FOR UPDATE", Integer.class, id);
    }

    public Map<String, Object> lockBooking(int id) {
        var rows=jdbc.queryForList("SELECT id FROM bookings WHERE id=? FOR UPDATE", id);
        return rows.isEmpty() ? null : basic(id);
    }

    public boolean overlaps(int vehicleId, LocalDate date, String slot, int durationHours, int excludedId) {
        int start=java.time.LocalTime.parse(slot).toSecondOfDay(), end=start+durationHours*3600;
        var rows=jdbc.queryForList("SELECT b.time_slot AS \"slot\", p.duration_hours AS \"hours\" FROM bookings b JOIN safari_packages p ON p.id=b.package_id WHERE b.vehicle_id=? AND b.safari_date=? AND b.status<>'CANCELLED' AND b.id<>?", vehicleId,date,excludedId);
        return rows.stream().anyMatch(row -> {
            int other=java.time.LocalTime.parse(row.get("slot").toString()).toSecondOfDay();
            return start < other+((Number)row.get("hours")).intValue()*3600 && other < end;
        });
    }

    public void edit(int id, LocalDate date, String slot, int guests, String requests, double total) {
        if(jdbc.queryForObject("SELECT COUNT(*) FROM booking_guide_assignments mine JOIN booking_guide_assignments other ON other.guide_id=mine.guide_id JOIN bookings b ON b.id=other.booking_id WHERE mine.booking_id=? AND b.id<>? AND b.safari_date=? AND b.status<>'CANCELLED'",Integer.class,id,id,date)>0)throw com.wildtrail.exception.ApiException.conflict("Assigned guide is unavailable on the requested date. Contact staff.");
        jdbc.update("UPDATE bookings SET safari_date=?,time_slot=?,guests=?,special_requests=?,total_amount=?,updated_at=NOW() WHERE id=?",date,slot,guests,requests,total,id);
        jdbc.update("UPDATE payments SET amount=? WHERE booking_id=? AND status='PENDING'",total,id);
    }

    public void updateBookingStatus(int id, String status) {
        this.jdbc.update("UPDATE bookings SET status=?, updated_at=NOW() WHERE id=?", new Object[]{status, id});
    }

    public boolean delete(int id) {
        return this.jdbc.update("DELETE FROM bookings WHERE id=?", new Object[]{id}) > 0;
    }
}

