package com.wildtrail.modules.payments;

import com.wildtrail.exception.ApiException;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PaymentController {
    private final com.wildtrail.modules.payments.PaymentService payments;
    public PaymentController(com.wildtrail.modules.payments.PaymentService payments){this.payments=payments;}
    @GetMapping(value={"/payments"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public List<Map<String, Object>> payments() {
        return this.payments.payments();
    }
    @PatchMapping(value={"/payments/{id}"})
    @PreAuthorize(value="hasRole('ADMIN')")
    public Map<String, Boolean> updatePaymentStatus(@PathVariable int id, @RequestBody Map<String, Object> body) {
        Object status = body.get("status");
        if (status == null) {
            throw ApiException.badRequest("Status is required");
        }
        this.payments.updatePaymentStatus(id, status.toString());
        return Map.of("ok", true);
    }
    @DeleteMapping("/payments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,Boolean> removeFailedPayment(@PathVariable int id) { payments.deleteFailedPayment(id); return Map.of("ok",true); }
}
