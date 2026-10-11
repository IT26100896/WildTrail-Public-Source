package com.wildtrail.modules.bookings;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.payments.Payment;
import com.wildtrail.modules.payments.PaymentFactory;
import com.wildtrail.modules.payments.PaymentResult;
import com.wildtrail.modules.safaris.SafariPackage;
import com.wildtrail.modules.resources.Vehicle;
import com.wildtrail.modules.users.JdbcCustomerRepository;
import com.wildtrail.security.SessionUser;
import com.wildtrail.modules.safaris.PackageService;
import com.wildtrail.util.RequestValues;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private static final List<String> PAYMENT_STATUSES = List.of("PENDING", "PAID", "REFUNDED", "FAILED");
    private final JdbcBookingRepository bookings;
    private final JdbcCustomerRepository people;
    private final PackageService catalog;
    private final com.wildtrail.modules.resources.VehicleService vehicles;
    private final com.wildtrail.modules.payments.JdbcPaymentRepository payments;

    public BookingService(JdbcBookingRepository bookings, JdbcCustomerRepository people, PackageService catalog, com.wildtrail.modules.resources.VehicleService vehicles, com.wildtrail.modules.payments.JdbcPaymentRepository payments) {
        this.bookings = bookings;
        this.people = people;
        this.catalog = catalog;
        this.vehicles=vehicles;this.payments=payments;
    }

    public Map<String, Double> quote(int packageId, int vehicleId, int guests) {
        SafariPackage safariPackage = this.catalog.requirePackage(packageId);
        Vehicle vehicle = this.vehicles.requireVehicle(vehicleId);
        safariPackage.validateGuests(guests);
        return Booking.quote(safariPackage, vehicle, guests);
    }

    public List<Integer> availableVehicleIds(String dateText, String slot, Integer packageId) {
        LocalDate date = LocalDate.parse(Booking.validateDate(dateText));
        if (slot == null || slot.isBlank()) {
            throw ApiException.badRequest("date and slot are required");
        }
        try { java.time.LocalTime.parse(slot); } catch (RuntimeException e) { throw ApiException.badRequest("Invalid departure time"); }
        int hours=24;
        if (packageId != null) {
            SafariPackage item=catalog.requirePackage(packageId);
            if (!item.isBookable()) throw ApiException.badRequest("This package is not currently bookable");
            Booking.validateSlot(item,slot);
            hours=item.getDurationHours();
        }
        final int duration=hours;
        return vehicles.vehicles(true).stream().map(v -> ((Number)v.get("id")).intValue()).filter(id -> !bookings.overlaps(id,date,slot,duration,0)).toList();
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body, SessionUser user) {
        int customerId;
        Map<String, Object> customer;
        int packageId = RequestValues.integer(body, "packageId", 1, Integer.MAX_VALUE);
        int vehicleId = RequestValues.integer(body, "vehicleId", 1, Integer.MAX_VALUE);
        int guests = RequestValues.integer(body, "guests", 1, 20);
        String safariDateText = RequestValues.text(body, "safariDate", 10);
        String safariDate = Booking.validateDate(safariDateText);
        SafariPackage safariPackage = this.catalog.requirePackage(packageId);
        Vehicle vehicle = this.vehicles.requireVehicle(vehicleId);
        String slot = RequestValues.optionalText(body, "timeSlot");
        if (slot.isBlank()) {
            slot = safariPackage.getStartTime();
        }
        Booking.validateSlot(safariPackage, slot);
        if (!safariPackage.isBookable()) {
            throw ApiException.badRequest("This package is not currently bookable");
        }
        safariPackage.validateGuests(guests);
        vehicle.ensureBookableFor(guests);
        this.bookings.lockVehicle(vehicleId);
        if (this.bookings.overlaps(vehicleId, LocalDate.parse(safariDate), slot, safariPackage.getDurationHours(), 0)) {
            throw ApiException.conflict(vehicle.getName() + " is already booked for " + safariDate + " at " + slot + ". Please choose another vehicle.");
        }
        Map<String, Double> quote = Booking.quote(safariPackage, vehicle, guests);
        String method = RequestValues.text(body, "paymentMethod", 10);
        if (!method.equals("CASH") && !(method.equals("CARD") && Boolean.TRUE.equals(body.get("cardDemo")))) throw ApiException.badRequest("Online payments are unavailable. Choose cash on arrival.");
        Payment payment = PaymentFactory.create(method, quote.get("total"), body);
        PaymentResult result = payment.process();
        String name = RequestValues.text(body, "fullName", 120);
        String email = RequestValues.email(body);
        String phone = RequestValues.text(body, "phone", 40);
        String country = RequestValues.optionalText(body, "country");
        if (user == null || !user.role().equals("ROLE_CUSTOMER")) throw ApiException.unauthorized("Please sign in before booking");
        if (!email.equalsIgnoreCase(user.email())) throw ApiException.forbidden("Booking email must match your account");
        customer = this.people.customerByUser(user.uid());
        if (customer == null) {
            customerId = this.people.createGuestCustomer(name, email, phone, country, user == null ? null : Integer.valueOf(user.uid()));
        } else {
            customerId = ((Number)customer.get("id")).intValue();
            this.people.updateCustomerContact(customerId, phone, country);
        }
        String status = "PAID".equals(result.status()) ? "CONFIRMED" : "PENDING";
        LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
        values.put("reference", this.newReference());
        values.put("customerId", customerId);
        values.put("packageId", packageId);
        values.put("vehicleId", vehicleId);
        values.put("safariDate", LocalDate.parse(safariDate));
        values.put("timeSlot", slot);
        values.put("guests", guests);
        String requests=RequestValues.optionalText(body,"specialRequests");
        if(requests.length()>1000 || country.length()>80) throw ApiException.badRequest("Booking details are too long");
        values.put("specialRequests", requests);
        values.put("totalAmount", quote.get("total"));
        values.put("status", status);
        int bookingId = this.bookings.create(values);
        this.payments.createPayment(bookingId, result.method(), result.amount(), result.status(), result.transactionRef(), result.cardLast4(), result.paidAt());
        return this.detailedByReference((String)values.get("reference"));
    }

    public List<Map<String, Object>> all() {
        return this.bookings.allDetailed();
    }

    public List<Map<String, Object>> mine(int userId) {
        Map<String, Object> customer = this.people.customerByUser(userId);
        if (customer == null) {
            return List.of();
        }
        return this.bookings.forCustomer(((Number)customer.get("id")).intValue());
    }

    @Transactional
    public Map<String, Object> cancelMine(int id, int userId) {
        Map<String, Object> row = this.bookings.lockBooking(id);
        if (row == null) {
            throw ApiException.notFound("Booking with id " + id + " was not found");
        }
        Map<String, Object> customer = this.people.customerByUser(userId);
        if (customer == null || ((Number)row.get("customerId")).intValue() != ((Number)customer.get("id")).intValue()) {
            throw ApiException.forbidden("This booking does not belong to you");
        }
        if (!List.of("PENDING", "CONFIRMED").contains(row.get("status"))) {
            throw ApiException.conflict("Only active bookings can be cancelled");
        }
        LocalDate safariDate = LocalDate.parse(row.get("safariDate").toString());
        long millisUntilSafari = safariDate.atTime(java.time.LocalTime.parse(row.get("timeSlot").toString())).atZone(ZoneId.of("Asia/Colombo")).toInstant().toEpochMilli() - System.currentTimeMillis();
        if (millisUntilSafari < 86400000L) {
            throw ApiException.badRequest("Bookings can only be cancelled at least 24 hours before the safari");
        }
        return this.updateStatus(id, "CANCELLED");
    }

    @Transactional
    public Map<String, Object> updateStatus(int id, String next) {
        Map<String, Object> payment;
        Map<String, Object> row = this.bookings.lockBooking(id);
        if (row == null) {
            throw ApiException.notFound("Booking with id " + id + " was not found");
        }
        if (!List.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED").contains(next)) {
            throw ApiException.badRequest("Invalid status");
        }
        Booking booking = new Booking((String)row.get("status"));
        booking.transitionTo(next);
        if(next.equals("COMPLETED")) {
            var receipt=payments.paymentForBooking(id);
            var departure=LocalDate.parse(row.get("safariDate").toString()).atTime(java.time.LocalTime.parse(row.get("timeSlot").toString())).atZone(ZoneId.of("Asia/Colombo")).toInstant();
            if(receipt==null || !"PAID".equals(receipt.get("status"))) throw ApiException.conflict("Record payment before completing the safari");
            if(departure.isAfter(java.time.Instant.now())) throw ApiException.conflict("A future safari cannot be marked completed");
        }
        this.bookings.updateBookingStatus(id, booking.getStatus());
        if ("CANCELLED".equals(booking.getStatus()) && (payment = this.payments.paymentForBooking(id)) != null) {
            if ("PENDING".equals(payment.get("status"))) this.payments.updatePaymentStatus(((Number)payment.get("id")).intValue(), "FAILED");
        }
        return this.detailedByReference((String)row.get("reference"));
    }

    @Transactional
    public void delete(int id) {
        var row=bookings.lockBooking(id);
        if(row==null) throw ApiException.notFound("Booking was not found");
        if(!"CANCELLED".equals(row.get("status"))) throw ApiException.conflict("Cancel a booking before deleting it");
        var payment=payments.paymentForBooking(id);
        if(payment!=null && "PAID".equals(payment.get("status"))) throw ApiException.conflict("Record the completed cash refund before deleting this booking");
        if (!this.bookings.delete(id)) {
            throw ApiException.notFound("Booking with id " + id + " was not found");
        }
    }

    @Transactional
    public Map<String,Object> editMine(int id, int userId, Map<String,Object> body) {
        bookings.lockGuideSchedule();
        var row=bookings.lockBooking(id);
        if(row==null) throw ApiException.notFound("Booking was not found");
        var customer=people.customerByUser(userId);
        if(customer==null || !row.get("customerId").equals(customer.get("id"))) throw ApiException.forbidden("This booking does not belong to you");
        if(!List.of("PENDING","CONFIRMED").contains(row.get("status"))) throw ApiException.conflict("Only active bookings can be changed");
        var item=catalog.requirePackage(((Number)row.get("packageId")).intValue());
        var vehicle=vehicles.requireVehicle(((Number)row.get("vehicleId")).intValue());
        java.time.Instant now = java.time.Instant.now();
        Booking.requireChangeNotice(LocalDate.parse(row.get("safariDate").toString()), row.get("timeSlot").toString(), now);
        LocalDate date=LocalDate.parse(Booking.validateDate(RequestValues.text(body,"safariDate",10)));
        String slot=RequestValues.text(body,"timeSlot",5); Booking.validateSlot(item,slot);
        Booking.requireChangeNotice(date, slot, now);
        int guests=RequestValues.integer(body,"guests",1,20);
        double total=Booking.quote(item,vehicle,guests).get("total");
        var payment=payments.paymentForBooking(id);
        if(payment!=null && "PAID".equals(payment.get("status")) && Double.compare(total,((Number)row.get("totalAmount")).doubleValue())!=0) throw ApiException.conflict("Paid bookings cannot change price. Contact staff.");
        bookings.lockVehicle(vehicle.getId());
        if(bookings.overlaps(vehicle.getId(),date,slot,item.getDurationHours(),id)) throw ApiException.conflict("Vehicle is unavailable for this departure");
        String requests=RequestValues.optionalText(body,"specialRequests"); if(requests.length()>1000) throw ApiException.badRequest("Special requests are too long");
        bookings.edit(id,date,slot,guests,requests,total);
        return detailedByReference(row.get("reference").toString());
    }

    private Map<String, Object> detailedByReference(String reference) {
        return this.bookings.byReference(reference).stream().findFirst().orElseThrow(() -> ApiException.notFound("Booking " + reference + " was not found"));
    }

    private String newReference() {
        return "WT-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
