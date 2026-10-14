package com.wildtrail.service;

import com.wildtrail.modules.bookings.JdbcBookingRepository;
import com.wildtrail.modules.users.JdbcCustomerRepository;
import com.wildtrail.modules.safaris.PackageService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final JdbcBookingRepository bookings;
    private final JdbcCustomerRepository people;
    private final PackageService catalog;
    private final com.wildtrail.modules.resources.VehicleService vehicles;
    private final com.wildtrail.modules.payments.JdbcPaymentRepository payments;
    private final com.wildtrail.modules.reviews.JdbcReviewRepository reviews;

    public AdminService(JdbcBookingRepository bookings, JdbcCustomerRepository people, PackageService catalog, com.wildtrail.modules.resources.VehicleService vehicles, com.wildtrail.modules.payments.JdbcPaymentRepository payments, com.wildtrail.modules.reviews.JdbcReviewRepository reviews) {
        this.bookings = bookings;
        this.people = people;
        this.catalog = catalog;
        this.vehicles=vehicles;this.payments=payments;this.reviews=reviews;
    }

    public Map<String, Object> dashboard() {
        List<Map<String, Object>> allBookings = this.bookings.allDetailed();
        List<Map<String, Object>> customers = this.people.customers();
        List<Map<String, Object>> payments = this.payments.payments();
        List<Map<String, Object>> reviews = this.reviews.reviews(false);
        LinkedHashMap<String, Integer> breakdown = new LinkedHashMap<String, Integer>();
        breakdown.put("PENDING", 0);
        breakdown.put("CONFIRMED", 0);
        breakdown.put("COMPLETED", 0);
        breakdown.put("CANCELLED", 0);
        LinkedHashMap<String, Double> revenueByPackage = new LinkedHashMap<String, Double>();
        for (Map<String, Object> booking : allBookings) {
            String status = booking.get("status").toString();
            breakdown.compute(status, (key, count) -> (count == null ? 0 : count) + 1);
            if (!"PAID".equals(booking.get("paymentStatus"))) continue;
            String packageName = booking.get("packageName").toString();
            double amount = ((Number)booking.get("totalAmount")).doubleValue();
            revenueByPackage.merge(packageName, amount, Double::sum);
        }
        List<Map<String, Object>> topPackages = revenueByPackage.entrySet().stream().map(entry -> Map.<String, Object>of("name", entry.getKey(), "total", entry.getValue())).sorted(Comparator.comparingDouble(row -> -((Number)row.get("total")).doubleValue())).limit(6L).toList();
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Colombo"));
        List<Map<String, Object>> upcoming = allBookings.stream().filter(row -> !this.date(row.get("safariDate")).isBefore(today)).filter(row -> !"CANCELLED".equals(row.get("status"))).sorted(Comparator.comparing(row -> this.date(row.get("safariDate")))).limit(5L).toList();
        List recent = allBookings.stream().limit(6L).toList();
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("totalBookings", allBookings.size());
        result.put("pendingBookings", breakdown.get("PENDING"));
        result.put("totalCustomers", customers.size());
        result.put("revenuePaid", this.sumPayments(payments, "PAID"));
        result.put("revenuePending", this.sumPayments(payments, "PENDING"));
        result.put("activePackages", this.catalog.packages(true).size());
        result.put("availableVehicles", this.vehicles.vehicles(true).size());
        result.put("publishedReviews", reviews.stream().filter(row -> !Boolean.TRUE.equals(row.get("sample"))).count());
        result.put("statusBreakdown", breakdown);
        result.put("revenueByPackage", topPackages);
        result.put("upcoming", upcoming);
        result.put("recent", recent);
        return result;
    }

    private double sumPayments(List<Map<String, Object>> payments, String status) {
        return payments.stream().filter(row -> status.equals(row.get("status"))).mapToDouble(row -> ((Number)row.get("amount")).doubleValue()).sum();
    }

    private LocalDate date(Object value) {
        LocalDate localDate;
        return value instanceof LocalDate ? (localDate = (LocalDate)value) : LocalDate.parse(value.toString());
    }
}
