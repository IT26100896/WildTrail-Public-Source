package com.wildtrail.modules.resources;

import com.wildtrail.exception.ApiException;
import com.wildtrail.util.RequestValues;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {
    private static final Set<String> PACKAGE_TYPES = Set.of("MORNING", "EVENING", "HALF_DAY", "FULL_DAY", "PRIVATE", "FAMILY", "PHOTOGRAPHY");
    private static final Set<String> VEHICLE_TYPES = Set.of("STANDARD", "HILUX", "LUXURY", "FAMILY");
    private final JdbcVehicleRepository catalog;

    public VehicleService(JdbcVehicleRepository catalog) {
        this.catalog = catalog;
    }

    public List<Map<String, Object>> vehicles(boolean availableOnly) {
        return this.catalog.vehicles(availableOnly);
    }

    public Map<String, Object> createVehicle(Map<String, Object> body) {
        return this.catalog.createVehicle(this.parseVehicle(body));
    }

    @Transactional
    public Map<String, Object> updateVehicle(int id, Map<String, Object> body) {
        Map<String, Object> values = this.parseVehicle(body);
        // Booking creation locks this same row before saving. Keep the lock until
        // this availability check and update have both finished.
        if (!this.catalog.lockVehicle(id)) {
            throw ApiException.notFound("Vehicle with id " + id + " was not found");
        }
        // Older clients omit this optional field; retain the recorded plate in that case.
        if (!body.containsKey("registrationNumber")) {
            this.catalog.vehicles(false).stream()
                    .filter(row -> ((Number)row.get("id")).intValue() == id)
                    .findFirst().ifPresent(row -> values.put("registrationNumber", row.get("registrationNumber")));
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Colombo"));
        if (this.catalog.hasIncompatibleActiveBooking(
                id, (int) values.get("capacity"), (boolean) values.get("available"), today)) {
            throw ApiException.conflict("Vehicle has active bookings that require its current availability and capacity");
        }
        if (!this.catalog.updateVehicle(id, values)) {
            throw ApiException.notFound("Vehicle with id " + id + " was not found");
        }
        return this.vehicles(false).stream().filter(row -> ((Number)row.get("id")).intValue() == id).findFirst().orElseThrow(() -> ApiException.notFound("Vehicle with id " + id + " was not found"));
    }

    public void deleteVehicle(int id) {
        if (!this.catalog.deleteVehicle(id)) {
            throw ApiException.notFound("Vehicle with id " + id + " was not found");
        }
    }

    public Vehicle requireVehicle(int id) {
        Vehicle vehicle = this.catalog.findVehicle(id);
        if (vehicle == null) {
            throw ApiException.notFound("Vehicle with id " + id + " was not found");
        }
        return vehicle;
    }

    private Map<String, Object> parseVehicle(Map<String, Object> body) {
        String category = RequestValues.text(body, "category", 20);
        if (!VEHICLE_TYPES.contains(category)) {
            throw ApiException.badRequest("Vehicle category is invalid");
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", RequestValues.text(body, "name", 120));
        String number = RequestValues.optionalText(body, "registrationNumber").toUpperCase(java.util.Locale.ROOT);
        if (number.length() > 32 || (!number.isBlank() && !number.matches("[A-Z0-9 -]+"))) {
            throw ApiException.badRequest("Vehicle number must contain letters, numbers, spaces or hyphens (max 32 characters)");
        }
        result.put("registrationNumber", number.isBlank() ? null : number);
        result.put("category", category);
        result.put("capacity", RequestValues.integer(body, "capacity", 1, 20));
        result.put("fourByFour", RequestValues.bool(body, "fourByFour", true));
        result.put("features", String.join((CharSequence)",", RequestValues.stringList(body, "features")));
        result.put("available", RequestValues.bool(body, "available", true));
        result.put("price", RequestValues.nonNegative(body, "price"));
        result.put("imageUrl", RequestValues.text(body, "imageUrl", 2000));
        return result;
    }
}
