package com.wildtrail.modules.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.wildtrail.repository.JdbcInsert;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcVehicleRepository {
    private static final String PACKAGE_SELECT = "SELECT p.id, p.name, p.package_type AS \"packageType\", p.destination_id AS \"destinationId\",\n       d.name AS \"destinationName\", p.duration_hours AS \"durationHours\", p.start_time AS \"startTime\",\n       p.max_guests AS \"maxGuests\", p.base_price AS \"basePrice\", p.description, p.image_url AS \"imageUrl\",\n       p.active\nFROM safari_packages p JOIN destinations d ON d.id = p.destination_id\n";
    private static final String VEHICLE_SELECT = "SELECT id, name, category, capacity, four_by_four AS \"fourByFour\", features,\n       available, price, registration_number AS \"registrationNumber\",\n       owner_name AS \"ownerName\", owner_phone AS \"ownerPhone\",\n       owner_address AS \"ownerAddress\", owner_id_number AS \"ownerIdNumber\", image_url AS \"imageUrl\"\nFROM vehicles\n";
    private final JdbcTemplate jdbc;

    public JdbcVehicleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> vehicles(boolean availableOnly) {
        String sql = VEHICLE_SELECT + (availableOnly ? " WHERE available=TRUE" : "") + " ORDER BY id";
        return this.jdbc.queryForList(sql).stream().map(row -> this.vehicleDto(row)).toList();
    }

    public Vehicle findVehicle(int id) {
        List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT id, name, category, capacity, four_by_four AS \"fourByFour\", features,\n       available, price, registration_number AS \"registrationNumber\",\n       owner_name AS \"ownerName\", owner_phone AS \"ownerPhone\",\n       owner_address AS \"ownerAddress\", owner_id_number AS \"ownerIdNumber\", image_url AS \"imageUrl\"\nFROM vehicles\n WHERE id=?", new Object[]{id});
        return rows.isEmpty() ? null : this.vehicleModel(rows.get(0));
    }

    public Map<String, Object> createVehicle(Map<String, Object> values) {
        Integer id = JdbcInsert.id(this.jdbc, "INSERT INTO vehicles(name, category, capacity, four_by_four, features, available, price, image_url, registration_number,\n    owner_name, owner_phone, owner_address, owner_id_number)\nVALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id\n", new Object[]{values.get("name"), values.get("category"), values.get("capacity"), values.get("fourByFour"), values.get("features"), values.get("available"), values.get("price"), values.get("imageUrl"), values.get("registrationNumber"), values.get("ownerName"), values.get("ownerPhone"), values.get("ownerAddress"), values.get("ownerIdNumber")});
        return vehicles(false).stream().filter(row -> ((Number)row.get("id")).intValue() == id).findFirst().orElseThrow();
    }

    public boolean updateVehicle(int id, Map<String, Object> values) {
        return this.jdbc.update("UPDATE vehicles SET name=?, category=?, capacity=?, four_by_four=?, features=?, available=?,\n    price=?, image_url=?, registration_number=?,\n    owner_name=?, owner_phone=?, owner_address=?, owner_id_number=?, updated_at=NOW() WHERE id=?\n", new Object[]{values.get("name"), values.get("category"), values.get("capacity"), values.get("fourByFour"), values.get("features"), values.get("available"), values.get("price"), values.get("imageUrl"), values.get("registrationNumber"), values.get("ownerName"), values.get("ownerPhone"), values.get("ownerAddress"), values.get("ownerIdNumber"), id}) > 0;
    }

    public boolean lockVehicle(int id) {
        return !this.jdbc.queryForList(
                "SELECT id FROM vehicles WHERE id=? FOR UPDATE", id).isEmpty();
    }

    public boolean hasIncompatibleActiveBooking(int id, int capacity,
                                                boolean available, LocalDate today) {
        String sql = """
                SELECT COUNT(*) FROM bookings
                WHERE vehicle_id = ? AND safari_date >= ?
                  AND status IN ('PENDING', 'CONFIRMED')
                  AND (guests > ? OR ? = FALSE)
                """;
        Integer count = this.jdbc.queryForObject(
                sql, Integer.class, id, today, capacity, available);
        return count != null && count > 0;
    }

    public boolean deleteVehicle(int id) {
        return this.jdbc.update("DELETE FROM vehicles WHERE id=?", new Object[]{id}) > 0;
    }

    private Vehicle vehicleModel(Map<String, Object> row) {
        String features = Objects.toString(row.get("features"), "");
        List<String> list = Arrays.stream(features.split(",")).map(String::trim).filter(f -> !f.isBlank()).toList();
        int id = JdbcVehicleRepository.integer(row, "id");
        String name = JdbcVehicleRepository.string(row, "name");
        int capacity = JdbcVehicleRepository.integer(row, "capacity");
        boolean fourByFour = Boolean.TRUE.equals(row.get("fourByFour"));
        boolean available = Boolean.TRUE.equals(row.get("available"));
        double price = JdbcVehicleRepository.decimal(row, "price");
        String image = JdbcVehicleRepository.string(row, "imageUrl");
        return switch (JdbcVehicleRepository.string(row, "category")) {
            case "STANDARD" -> new SafariJeep(id, name, capacity, fourByFour, list, available, price, image);
            case "HILUX" -> new HiluxSafariJeep(id, name, capacity, fourByFour, list, available, price, image);
            case "LUXURY" -> new LuxuryJeep(id, name, capacity, fourByFour, list, available, price, image);
            case "FAMILY" -> new FamilySafariJeep(id, name, capacity, fourByFour, list, available, price, image);
            default -> throw new IllegalStateException("Unsupported vehicle category in database");
        };
    }

    private Map<String, Object> vehicleDto(Map<String, Object> row) {
        Vehicle vehicle = vehicleModel(row);
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("id", vehicle.getId());
        result.put("name", vehicle.getName());
        result.put("registrationNumber", row.get("registrationNumber"));
        result.put("ownerName", row.get("ownerName"));
        result.put("ownerPhone", row.get("ownerPhone"));
        result.put("ownerAddress", row.get("ownerAddress"));
        result.put("ownerIdNumber", row.get("ownerIdNumber"));
        result.put("capacity", vehicle.getCapacity());
        result.put("fourByFour", vehicle.isFourByFour());
        result.put("features", vehicle.getFeatures());
        result.put("available", vehicle.isAvailable());
        result.put("price", vehicle.getPrice());
        result.put("imageUrl", vehicle.getImageUrl());
        result.put("category", vehicle.getCategory());
        result.put("categoryLabel", vehicle.getCategoryLabel());
        result.put("allFeatures", vehicle.getAllFeatures());
        return result;
    }

    private static int integer(Map<String, Object> row, String key) {
        return ((Number)row.get(key)).intValue();
    }

    private static double decimal(Map<String, Object> row, String key) {
        double d;
        Object value = row.get(key);
        if (value instanceof BigDecimal) {
            BigDecimal number = (BigDecimal)value;
            d = number.doubleValue();
        } else {
            d = ((Number)value).doubleValue();
        }
        return d;
    }

    private static String string(Map<String, Object> row, String key) {
        return Objects.toString(row.get(key), "");
    }
}

