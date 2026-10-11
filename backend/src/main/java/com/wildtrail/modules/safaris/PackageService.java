package com.wildtrail.modules.safaris;

import com.wildtrail.exception.ApiException;
import com.wildtrail.util.RequestValues;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PackageService {
    private static final Set<String> PACKAGE_TYPES = Set.of("MORNING", "EVENING", "HALF_DAY", "FULL_DAY", "PRIVATE", "FAMILY", "PHOTOGRAPHY");
    private static final Set<String> VEHICLE_TYPES = Set.of("STANDARD", "HILUX", "LUXURY", "FAMILY");
    private final JdbcPackageRepository catalog;

    public PackageService(JdbcPackageRepository catalog) {
        this.catalog = catalog;
    }

    public List<Map<String, Object>> destinations() {
        return this.catalog.destinations();
    }

    public List<Map<String, Object>> packages(boolean activeOnly) {
        return this.catalog.packages(activeOnly);
    }

    public Map<String, Object> packageById(int id) {
        SafariPackage item = this.catalog.findPackage(id);
        if (item == null) {
            throw ApiException.notFound("Safari package with id " + id + " was not found");
        }
        return this.catalog.packages(false).stream().filter(row -> ((Number)row.get("id")).intValue() == id).findFirst().orElseThrow(() -> ApiException.notFound("Safari package with id " + id + " was not found"));
    }

    public Map<String, Object> createPackage(Map<String, Object> body) {
        return this.catalog.createPackage(this.parsePackage(body));
    }

    public Map<String, Object> updatePackage(int id, Map<String, Object> body) {
        Map<String, Object> values = this.parsePackage(body);
        if (!this.catalog.updatePackage(id, values)) {
            throw ApiException.notFound("Safari package with id " + id + " was not found");
        }
        return this.packageById(id);
    }

    public void deletePackage(int id) {
        if (!this.catalog.deletePackage(id)) {
            throw ApiException.notFound("Safari package with id " + id + " was not found");
        }
    }

    public SafariPackage requirePackage(int id) {
        SafariPackage item = this.catalog.findPackage(id);
        if (item == null) {
            throw ApiException.notFound("Safari package with id " + id + " was not found");
        }
        return item;
    }

    private Map<String, Object> parsePackage(Map<String, Object> body) {
        String type = RequestValues.text(body, "packageType", 20);
        if (!PACKAGE_TYPES.contains(type)) {
            throw ApiException.badRequest("Package type is invalid");
        }
        int destinationId = RequestValues.integer(body, "destinationId", 1, Integer.MAX_VALUE);
        if (!this.catalog.destinationExists(destinationId)) {
            throw ApiException.badRequest("Destination does not exist");
        }
        String startTime = RequestValues.text(body, "startTime", 10);
        if (!startTime.matches("\\d{2}:\\d{2}")) {
            throw ApiException.badRequest("Start time must be HH:MM");
        }
        java.time.LocalTime departure;
        try { departure = java.time.LocalTime.parse(startTime); } catch(RuntimeException e) { throw ApiException.badRequest("Start time must be a valid HH:MM time"); }
        int durationHours = RequestValues.integer(body, "durationHours", 1, 24);
        if (departure.toSecondOfDay() + durationHours * 3600 > 24 * 3600) {
            throw ApiException.badRequest("Safari must finish on the departure date");
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("name", RequestValues.text(body, "name", 120));
        result.put("packageType", type);
        result.put("destinationId", destinationId);
        result.put("durationHours", durationHours);
        result.put("startTime", startTime);
        result.put("maxGuests", RequestValues.integer(body, "maxGuests", 1, 20));
        result.put("basePrice", RequestValues.nonNegative(body, "basePrice"));
        result.put("description", RequestValues.text(body, "description", 1000));
        result.put("imageUrl", RequestValues.text(body, "imageUrl", 2000));
        result.put("active", RequestValues.bool(body, "active", true));
        return result;
    }

}
