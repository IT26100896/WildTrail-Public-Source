
package com.wildtrail.modules.safaris;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.wildtrail.repository.JdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPackageRepository {

    // SQL query to get safari packages and destination details
    private static final String PACKAGE_SELECT =
        "SELECT p.id, p.name, p.package_type AS \"packageType\", " +
        "p.destination_id AS \"destinationId\", " +
        "d.name AS \"destinationName\", " +
        "p.duration_hours AS \"durationHours\", " +
        "p.start_time AS \"startTime\", " +
        "p.max_guests AS \"maxGuests\", " +
        "p.base_price AS \"basePrice\", " +
        "p.description, p.image_url AS \"imageUrl\", p.active " +
        "FROM safari_packages p " +
        "JOIN destinations d ON d.id = p.destination_id ";

    // SQL query to get vehicle details
    private static final String VEHICLE_SELECT =
        "SELECT id, name, category, capacity, " +
        "four_by_four AS \"fourByFour\", features, " +
        "available, price, image_url AS \"imageUrl\" " +
        "FROM vehicles ";

    // JdbcTemplate is used to communicate with the database
    private final JdbcTemplate jdbc;

    public JdbcPackageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    // 1. Get all destinations
    public List<Map<String, Object>> destinations() {

        String sql =
            "SELECT id, slug, name, location, description, " +
            "best_wildlife AS \"bestWildlife\", " +
            "safari_types AS \"safariTypes\", " +
            "starting_price AS \"startingPrice\", " +
            "image_url AS \"imageUrl\" " +
            "FROM destinations ORDER BY id";

        return jdbc.queryForList(sql);
    }


    // 2. Get safari packages
    public List<Map<String, Object>> packages(boolean activeOnly) {

        String sql = PACKAGE_SELECT;

        // Get only active packages if activeOnly is true
        if (activeOnly) {
            sql += " WHERE p.active = TRUE";
        }

        sql += " ORDER BY p.id";

        return jdbc.queryForList(sql)
                   .stream()
                   .map(this::packageDto)
                   .toList();
    }


    // 3. Find one package using its ID
    public SafariPackage findPackage(int id) {

        String sql = PACKAGE_SELECT + " WHERE p.id = ?";

        List<Map<String, Object>> rows =
            jdbc.queryForList(sql, new Object[]{id});

        if (rows.isEmpty()) {
            return null;
        }

        return packageModel(rows.get(0));
    }


    // 4. Create a new package
    public Map<String, Object> createPackage(Map<String, Object> values) {

        String sql =
            "INSERT INTO safari_packages " +
            "(name, package_type, destination_id, duration_hours, " +
            "start_time, max_guests, base_price, description, image_url, active) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        Object[] parameters = {
            values.get("name"),
            values.get("packageType"),
            values.get("destinationId"),
            values.get("durationHours"),
            values.get("startTime"),
            values.get("maxGuests"),
            values.get("basePrice"),
            values.get("description"),
            values.get("imageUrl"),
            values.get("active")
        };

        Integer id = JdbcInsert.id(jdbc, sql, parameters);

        SafariPackage safariPackage =
            packageModel(Objects.requireNonNull(id));

        return packageDto(safariPackage);
    }


    // 5. Update an existing package
    public boolean updatePackage(int id, Map<String, Object> values) {

        String sql =
            "UPDATE safari_packages SET " +
            "name=?, package_type=?, destination_id=?, " +
            "duration_hours=?, start_time=?, max_guests=?, " +
            "base_price=?, description=?, image_url=?, active=?, " +
            "updated_at=NOW() WHERE id=?";

        Object[] parameters = {
            values.get("name"),
            values.get("packageType"),
            values.get("destinationId"),
            values.get("durationHours"),
            values.get("startTime"),
            values.get("maxGuests"),
            values.get("basePrice"),
            values.get("description"),
            values.get("imageUrl"),
            values.get("active"),
            id
        };

        return jdbc.update(sql, parameters) > 0;
    }


    // 6. Delete a package
    public boolean deletePackage(int id) {

        String sql = "DELETE FROM safari_packages WHERE id=?";

        return jdbc.update(sql, new Object[]{id}) > 0;
    }


    // 7. Check whether a destination exists
    public boolean destinationExists(int id) {

        String sql = "SELECT COUNT(*) FROM destinations WHERE id=?";

        Integer count = jdbc.queryForObject(
            sql, Integer.class, new Object[]{id}
        );

        return count != null && count > 0;
    }


    // 8. Convert database data into a Map
    private Map<String, Object> packageDto(Map<String, Object> row) {

        SafariPackage safariPackage = packageModel(row);

        return packageDto(safariPackage);
    }


    // Convert SafariPackage object into a Map
    private Map<String, Object> packageDto(SafariPackage safariPackage) {

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        result.put("id", safariPackage.getId());
        result.put("name", safariPackage.getName());
        result.put("destinationId", safariPackage.getDestinationId());
        result.put("destinationName", safariPackage.getDestinationName());
        result.put("durationHours", safariPackage.getDurationHours());
        result.put("startTime", safariPackage.getStartTime());
        result.put("maxGuests", safariPackage.getMaxGuests());
        result.put("basePrice", safariPackage.getBasePrice());
        result.put("description", safariPackage.getDescription());
        result.put("imageUrl", safariPackage.getImageUrl());
        result.put("active", safariPackage.isActive());

        result.put("type", safariPackage.getType());
        result.put("typeLabel", safariPackage.getTypeLabel());
        result.put("pricingNote", safariPackage.getPricingNote());
        result.put("inclusions", List.of(safariPackage.getInclusions()));

        return result;
    }


    // 9. Find a package and convert it into a Java object
    private SafariPackage packageModel(int id) {

        String sql = PACKAGE_SELECT + " WHERE p.id=?";

        Map<String, Object> row =
            jdbc.queryForMap(sql, new Object[]{id});

        return packageModel(row);
    }


    // 10. Convert database information into the correct SafariPackage object
    private SafariPackage packageModel(Map<String, Object> row) {

        int id = integer(row, "id");
        String name = string(row, "name");

        int destinationId = integer(row, "destinationId");
        String destinationName = string(row, "destinationName");

        int duration = integer(row, "durationHours");
        String startTime = string(row, "startTime");

        int guests = integer(row, "maxGuests");
        double price = decimal(row, "basePrice");

        String description = string(row, "description");
        String image = string(row, "imageUrl");

        boolean active = Boolean.TRUE.equals(row.get("active"));


        // Polymorphism and Inheritance:
        // Create different objects depending on package type

        String packageType = string(row, "packageType");

        switch (packageType) {

            case "MORNING":
                return new MorningSafari(
                    id, name, destinationId, destinationName,
                    duration, startTime, guests, price,
                    description, image, active
                );

            case "EVENING":
                return new EveningSafari(
                    id, name, destinationId, destinationName,
                    duration, startTime, guests, price,
                    description, image, active
                );

            case "HALF_DAY":
                return new HalfDaySafari(
                    id, name, destinationId, destinationName,
                    duration, startTime, guests, price,
                    description, image, active
                );

            case "FULL_DAY":
                return new FullDaySafari(
                    id, name, destinationId, destinationName,
                    duration, startTime, guests, price,
                    description, image, active
                );

            case "PRIVATE":
                return new PrivateSafari(
                    id, name, destinationId, destinationName,
                    duration, startTime, guests, price,
                    description, image, active
                );

            case "FAMILY":
                return new FamilySafari(
                    id, name, destinationId, destinationName,
                    duration, startTime, guests, price,
                    description, image, active
                );

            case "PHOTOGRAPHY":
                return new PhotographySafari(
                    id, name, destinationId, destinationName,
                    duration, startTime, guests, price,
                    description, image, active
                );

            default:
                throw new IllegalStateException(
                    "Unsupported safari package type in database"
                );
        }
    }


    // Convert a database value into an integer
    private static int integer(Map<String, Object> row, String key) {

        return ((Number) row.get(key)).intValue();
    }


    // Convert a database value into a double
    private static double decimal(Map<String, Object> row, String key) {

        Object value = row.get(key);

        if (value instanceof BigDecimal) {

            BigDecimal number = (BigDecimal) value;

            return number.doubleValue();
        }

        return ((Number) value).doubleValue();
    }


    // Convert a database value into a String
    private static String string(Map<String, Object> row, String key) {

        return Objects.toString(row.get(key), "");
    }

}
