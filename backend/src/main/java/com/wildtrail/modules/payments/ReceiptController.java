package com.wildtrail.modules.payments;
import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.bookings.JdbcBookingRepository;
import com.wildtrail.modules.users.JdbcCustomerRepository;
import com.wildtrail.security.SessionUser;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
public class ReceiptController {
    private final JdbcBookingRepository bookings; private final JdbcCustomerRepository people;
    public ReceiptController(JdbcBookingRepository bookings,JdbcCustomerRepository people){this.bookings=bookings;this.people=people;}
    @GetMapping("/api/bookings/{id}/receipt") @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public Map<String,Object> receipt(@PathVariable int id,Authentication auth){
        var row=bookings.basic(id);if(row==null)throw ApiException.notFound("Booking was not found");
        SessionUser user=(SessionUser)auth.getPrincipal();
        if(!user.role().equals("ROLE_ADMIN")){var customer=people.customerByUser(user.uid());if(customer==null||!customer.get("id").equals(row.get("customerId")))throw ApiException.forbidden("This receipt does not belong to you");}
        return bookings.byReference(row.get("reference").toString()).get(0);
    }
}
