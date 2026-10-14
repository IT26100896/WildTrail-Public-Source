package com.wildtrail.modules.resources;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api"})
public class VehicleController {
    private final VehicleService catalog;

    public VehicleController(VehicleService catalog) {
        this.catalog = catalog;
    }

    @GetMapping(value={"/vehicles"})
    public List<Map<String, Object>> vehicles(@RequestParam(defaultValue="0") String all, Authentication authentication) {
        boolean showAll = "1".equals(all) && this.isAdmin(authentication);
        List<Map<String, Object>> vehicles = this.catalog.vehicles(!showAll);
        if (showAll) return vehicles;
        // Owner contact and identity information is restricted to the staff fleet view.
        return vehicles.stream().map(vehicle -> {
            Map<String, Object> publicVehicle = new java.util.LinkedHashMap<>(vehicle);
            for (String field : List.of("ownerName", "ownerPhone", "ownerAddress", "ownerIdNumber")) {
                publicVehicle.remove(field);
            }
            return publicVehicle;
        }).toList();
    }

    @PostMapping(value={"/vehicles"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createVehicle(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status((int)201).body(this.catalog.createVehicle(body));
    }

    @PutMapping(value={"/vehicles/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Object> updateVehicle(@PathVariable int id, @RequestBody Map<String, Object> body) {
        return this.catalog.updateVehicle(id, body);
    }

    @DeleteMapping(value={"/vehicles/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Boolean> deleteVehicle(@PathVariable int id) {
        this.catalog.deleteVehicle(id);
        return Map.of("ok", true);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
