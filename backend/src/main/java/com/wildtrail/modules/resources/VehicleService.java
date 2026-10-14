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
        // Omitted metadata stays unchanged; explicitly empty fields clear the recorded value.
        this.catalog.vehicles(false).stream()
                .filter(row -> ((Number)row.get("id")).intValue() == id)
                .findFirst().ifPresent(row -> {
                    for (String field : List.of("registrationNumber", "ownerName", "ownerPhone", "ownerAddress", "ownerIdNumber")) {
                        if (!body.containsKey(field)) values.put(field, row.get(field));
                    }
                });
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
        result.put("ownerName", optionalOwnerField(body, "ownerName", 120));
        result.put("ownerAddress", optionalOwnerField(body, "ownerAddress", 300));
        String phone = optionalOwnerField(body, "ownerPhone", 40);
        if (phone != null) {
            int digits = phone.replaceAll("[^0-9]", "").length();
            if (!phone.matches("\\+?[0-9 ()-]+") || digits < 7 || digits > 15) {
                throw ApiException.badRequest("Owner telephone number must contain 7 to 15 digits");
            }
        }
        result.put("ownerPhone", phone);
        String ownerId = optionalOwnerField(body, "ownerIdNumber", 40);
        if (ownerId != null) {
            ownerId = ownerId.toUpperCase(java.util.Locale.ROOT);
            if (!ownerId.matches("[A-Z0-9][A-Z0-9 -]*")) {
                throw ApiException.badRequest("Owner ID / NIC must contain letters, numbers, spaces or hyphens");
            }
        }
        result.put("ownerIdNumber", ownerId);
        result.put("category", category);
        result.put("capacity", RequestValues.integer(body, "capacity", 1, 20));
        result.put("fourByFour", RequestValues.bool(body, "fourByFour", true));
        result.put("features", String.join((CharSequence)",", RequestValues.stringList(body, "features")));
        result.put("available", RequestValues.bool(body, "available", true));
        result.put("price", RequestValues.nonNegative(body, "price"));
        result.put("imageUrl", RequestValues.text(body, "imageUrl", 2000));
        return result;
    }

    private String optionalOwnerField(Map<String, Object> body, String field, int maxLength) {
        String value = RequestValues.optionalText(body, field);
        if (value.length() > maxLength) throw ApiException.badRequest("Owner details are too long: " + field);
        return value.isBlank() ? null : value;
    }
}
