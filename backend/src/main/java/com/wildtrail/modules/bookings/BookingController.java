package com.wildtrail.modules.bookings;

import com.wildtrail.exception.ApiException;
import com.wildtrail.security.SessionUser;
import com.wildtrail.util.RequestValues;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Maps HTTP booking requests to BookingService; business rules stay in the service. */
@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookings;

    public BookingController(BookingService bookings) {
        this.bookings = bookings;
    }

    @GetMapping(value={"/bookings"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public List<Map<String, Object>> all() {
        // Staff view: includes customer, package, vehicle and payment details.
        return this.bookings.all();
    }

    @PostMapping(value={"/bookings"})
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, Authentication authentication) {
        // Pass the signed-in customer to the service so it can check ownership.
        SessionUser user = null;
        if (authentication != null && authentication.getPrincipal() instanceof SessionUser sessionUser) {
            user = sessionUser;
        }
        return ResponseEntity.status(201).body(this.bookings.create(body, user));
    }

    @GetMapping(value={"/bookings/mine"})
    @PreAuthorize(value="hasAnyRole('ADMIN','CUSTOMER')")
    public List<Map<String, Object>> mine(Authentication authentication) {
        return this.bookings.mine(((SessionUser)authentication.getPrincipal()).uid());
    }

    @PostMapping(value={"/bookings/mine"})
    @PreAuthorize(value="hasAnyRole('ADMIN','CUSTOMER')")
    public Map<String, Object> cancelMine(@RequestBody Map<String, Object> body, Authentication authentication) {
        // The existing client sends the booking ID in a POST body.
        Object id = body.get("id");
        try {
            int bookingId = Integer.parseInt(id.toString());
            if (bookingId < 1) {
                throw new NumberFormatException();
            }
            return this.bookings.cancelMine(bookingId, ((SessionUser)authentication.getPrincipal()).uid());
        }
        catch (RuntimeException exception) {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException)exception;
                throw apiException;
            }
            throw ApiException.badRequest("Id must be a whole number");
        }
    }

    @GetMapping(value={"/bookings/quote"})
    public Map<String, ?> quote(@RequestParam(required = false) String packageId,
                                @RequestParam(required = false) String vehicleId,
                                @RequestParam(required = false) String guests,
                                @RequestParam(required = false) String availability,
                                @RequestParam(required = false) String date,
                                @RequestParam(required = false) String slot) {
        // One endpoint serves availability checks and price quotes.
        if ("1".equals(availability)) {
            if (date == null || slot == null) {
                throw ApiException.badRequest("date and slot are required");
            }
            try {
                LocalDate.parse(date);
            }
            catch (DateTimeParseException exception) {
                throw ApiException.badRequest("Invalid safari date");
            }
            Integer pkg = packageId == null ? null
                    : RequestValues.integer(Map.of("packageId", packageId),
                            "packageId", 1, Integer.MAX_VALUE);
            return Map.of("availableVehicleIds", this.bookings.availableVehicleIds(date, slot, pkg));
        }
        LinkedHashMap<String, Object> query = new LinkedHashMap<String, Object>();
        query.put("packageId", packageId);
        query.put("vehicleId", vehicleId);
        query.put("guests", guests);
        int pkg = RequestValues.integer(query, "packageId", 1, Integer.MAX_VALUE);
        int vehicle = RequestValues.integer(query, "vehicleId", 1, Integer.MAX_VALUE);
        int count = RequestValues.integer(query, "guests", 1, 20);
        return this.bookings.quote(pkg, vehicle, count);
    }

    @PatchMapping(value={"/bookings/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Object> updateStatus(@PathVariable int id, @RequestBody Map<String, Object> body) {
        Object status = body.get("status");
        if (status == null) {
            throw ApiException.badRequest("Status is required");
        }
        return this.bookings.updateStatus(id, status.toString());
    }

    @DeleteMapping(value={"/bookings/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Boolean> delete(@PathVariable int id) {
        this.bookings.delete(id);
        return Map.of("ok", true);
    }

    @PutMapping("/bookings/mine/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public Map<String, Object> editMine(@PathVariable int id,
                                        @RequestBody Map<String, Object> body,
                                        Authentication authentication) {
        // A customer may change only a booking owned by that account.
        int userId = ((SessionUser) authentication.getPrincipal()).uid();
        return bookings.editMine(id, userId, body);
    }

}
