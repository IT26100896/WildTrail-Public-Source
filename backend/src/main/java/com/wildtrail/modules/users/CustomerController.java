package com.wildtrail.modules.users;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api"})
public class CustomerController {
    private final CustomerService people;

    public CustomerController(CustomerService people) {
        this.people = people;
    }

    @GetMapping(value={"/customers"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public List<Map<String, Object>> customers() {
        return this.people.customers();
    }

    @PutMapping(value={"/customers/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Object> updateCustomer(@PathVariable int id, @RequestBody Map<String, Object> body) {
        return this.people.updateCustomer(id, body);
    }

    @DeleteMapping(value={"/customers/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Boolean> deleteCustomer(@PathVariable int id) {
        this.people.deleteCustomer(id);
        return Map.of("ok", true);
    }

}
