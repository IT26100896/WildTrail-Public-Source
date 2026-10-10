package com.wildtrail.modules.safaris;

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
public class PackageController {
    private final PackageService catalog;

    public PackageController(PackageService catalog) {
        this.catalog = catalog;
    }

    @GetMapping(value={"/destinations"})
    public List<Map<String, Object>> destinations() {
        return this.catalog.destinations();
    }

    @GetMapping(value={"/packages"})
    public List<Map<String, Object>> packages(@RequestParam(defaultValue="0") String all, Authentication authentication) {
        boolean showAll = "1".equals(all) && this.isAdmin(authentication);
        return this.catalog.packages(!showAll);
    }

    @GetMapping(value={"/packages/{id}"})
    public Map<String, Object> packageById(@PathVariable int id) {
        return this.catalog.packageById(id);
    }

    @PostMapping(value={"/packages"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createPackage(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status((int)201).body(this.catalog.createPackage(body));
    }

    @PutMapping(value={"/packages/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Object> updatePackage(@PathVariable int id, @RequestBody Map<String, Object> body) {
        return this.catalog.updatePackage(id, body);
    }

    @DeleteMapping(value={"/packages/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Boolean> deletePackage(@PathVariable int id) {
        this.catalog.deletePackage(id);
        return Map.of("ok", true);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
