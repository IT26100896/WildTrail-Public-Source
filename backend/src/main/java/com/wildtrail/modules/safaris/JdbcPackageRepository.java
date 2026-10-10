package com.wildtrail.modules.safaris;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.wildtrail.repository.JdbcInsert;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPackageRepository {
    private static final String PACKAGE_SELECT = "SELECT p.id, p.name, p.package_type AS \"packageType\", p.destination_id AS \"destinationId\",\n       d.name AS \"destinationName\", p.duration_hours AS \"durationHours\", p.start_time AS \"startTime\",\n       p.max_guests AS \"maxGuests\", p.base_price AS \"basePrice\", p.description, p.image_url AS \"imageUrl\",\n       p.active\nFROM safari_packages p JOIN destinations d ON d.id = p.destination_id\n";
    private static final String VEHICLE_SELECT = "SELECT id, name, category, capacity, four_by_four AS \"fourByFour\", features,\n       available, price, image_url AS \"imageUrl\"\nFROM vehicles\n";
    private final JdbcTemplate jdbc;

    public JdbcPackageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> destinations() {
        return this.jdbc.queryForList("SELECT id, slug, name, location, description, best_wildlife AS \"bestWildlife\",\n       safari_types AS \"safariTypes\", starting_price AS \"startingPrice\", image_url AS \"imageUrl\"\nFROM destinations ORDER BY id\n");
    }

    public List<Map<String, Object>> packages(boolean activeOnly) {
        String sql = PACKAGE_SELECT + (activeOnly ? " WHERE p.active = TRUE" : "") + " ORDER BY p.id";
        return this.jdbc.queryForList(sql).stream().map(this::packageDto).toList();
    }

    public SafariPackage findPackage(int id) {
        List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT p.id, p.name, p.package_type AS \"packageType\", p.destination_id AS \"destinationId\",\n       d.name AS \"destinationName\", p.duration_hours AS \"durationHours\", p.start_time AS \"startTime\",\n       p.max_guests AS \"maxGuests\", p.base_price AS \"basePrice\", p.description, p.image_url AS \"imageUrl\",\n       p.active\nFROM safari_packages p JOIN destinations d ON d.id = p.destination_id\n WHERE p.id = ?", new Object[]{id});
        return rows.isEmpty() ? null : this.packageModel(rows.get(0));
    }

    public Map<String, Object> createPackage(Map<String, Object> values) {
        Integer id = JdbcInsert.id(this.jdbc, "INSERT INTO safari_packages(name, package_type, destination_id, duration_hours, start_time,\n    max_guests, base_price, description, image_url, active)\nVALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id\n", new Object[]{values.get("name"), values.get("packageType"), values.get("destinationId"), values.get("durationHours"), values.get("startTime"), values.get("maxGuests"), values.get("basePrice"), values.get("description"), values.get("imageUrl"), values.get("active")});
        return this.packageDto(this.packageModel(Objects.requireNonNull(id)));
    }

    public boolean updatePackage(int id, Map<String, Object> values) {
        return this.jdbc.update("UPDATE safari_packages SET name=?, package_type=?, destination_id=?, duration_hours=?, start_time=?,\n    max_guests=?, base_price=?, description=?, image_url=?, active=?, updated_at=NOW()\nWHERE id=?\n", new Object[]{values.get("name"), values.get("packageType"), values.get("destinationId"), values.get("durationHours"), values.get("startTime"), values.get("maxGuests"), values.get("basePrice"), values.get("description"), values.get("imageUrl"), values.get("active"), id}) > 0;
    }

    public boolean deletePackage(int id) {
        return this.jdbc.update("DELETE FROM safari_packages WHERE id=?", new Object[]{id}) > 0;
    }

    public boolean destinationExists(int id) {
        Integer count = (Integer)this.jdbc.queryForObject("SELECT COUNT(*) FROM destinations WHERE id=?", Integer.class, new Object[]{id});
        return count != null && count > 0;
    }

    private Map<String, Object> packageDto(Map<String, Object> row) {
        return this.packageDto(this.packageModel(row));
    }

    private Map<String, Object> packageDto(SafariPackage safariPackage) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
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

    private SafariPackage packageModel(int id) {
        Map row = this.jdbc.queryForMap("SELECT p.id, p.name, p.package_type AS \"packageType\", p.destination_id AS \"destinationId\",\n       d.name AS \"destinationName\", p.duration_hours AS \"durationHours\", p.start_time AS \"startTime\",\n       p.max_guests AS \"maxGuests\", p.base_price AS \"basePrice\", p.description, p.image_url AS \"imageUrl\",\n       p.active\nFROM safari_packages p JOIN destinations d ON d.id = p.destination_id\n WHERE p.id=?", new Object[]{id});
        return this.packageModel(row);
    }

    private SafariPackage packageModel(Map<String, Object> row) {
        int id = JdbcPackageRepository.integer(row, "id");
        String name = JdbcPackageRepository.string(row, "name");
        int destinationId = JdbcPackageRepository.integer(row, "destinationId");
        String destinationName = JdbcPackageRepository.string(row, "destinationName");
        int duration = JdbcPackageRepository.integer(row, "durationHours");
        String startTime = JdbcPackageRepository.string(row, "startTime");
        int guests = JdbcPackageRepository.integer(row, "maxGuests");
        double price = JdbcPackageRepository.decimal(row, "basePrice");
        String description = JdbcPackageRepository.string(row, "description");
        String image = JdbcPackageRepository.string(row, "imageUrl");
        boolean active = Boolean.TRUE.equals(row.get("active"));
        return switch (JdbcPackageRepository.string(row, "packageType")) {
            case "MORNING" -> new MorningSafari(id, name, destinationId, destinationName, duration, startTime, guests, price, description, image, active);
            case "EVENING" -> new EveningSafari(id, name, destinationId, destinationName, duration, startTime, guests, price, description, image, active);
            case "HALF_DAY" -> new HalfDaySafari(id, name, destinationId, destinationName, duration, startTime, guests, price, description, image, active);
            case "FULL_DAY" -> new FullDaySafari(id, name, destinationId, destinationName, duration, startTime, guests, price, description, image, active);
            case "PRIVATE" -> new PrivateSafari(id, name, destinationId, destinationName, duration, startTime, guests, price, description, image, active);
            case "FAMILY" -> new FamilySafari(id, name, destinationId, destinationName, duration, startTime, guests, price, description, image, active);
            case "PHOTOGRAPHY" -> new PhotographySafari(id, name, destinationId, destinationName, duration, startTime, guests, price, description, image, active);
            default -> throw new IllegalStateException("Unsupported safari package type in database");
        };
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

