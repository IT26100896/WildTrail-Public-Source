package com.wildtrail.modules.resources;

import com.wildtrail.exception.ApiException;
import com.wildtrail.util.RequestValues;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

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

    public Map<String, Object> updateVehicle(int id, Map<String, Object> body) {
        Map<String, Object> values = this.parseVehicle(body);
        if (this.catalog.hasIncompatibleActiveBooking(id, (int) values.get("capacity"), (boolean) values.get("available"))) {
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
