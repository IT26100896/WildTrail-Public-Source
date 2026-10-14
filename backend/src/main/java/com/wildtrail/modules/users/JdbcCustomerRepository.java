package com.wildtrail.modules.users;

import java.util.List;
import java.util.Map;
import com.wildtrail.repository.JdbcInsert;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCustomerRepository {
    private final JdbcTemplate jdbc;

    public JdbcCustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> customers() {
        return this.jdbc.queryForList("SELECT c.id, c.user_id AS \"userId\", c.full_name AS \"fullName\", c.email, c.phone, c.country,\n       c.created_at AS \"createdAt\",\n       COUNT(b.id)::int AS \"bookingCount\",\n       COALESCE(SUM(CASE WHEN p.status = 'PAID' THEN p.amount ELSE 0 END), 0)\n           AS \"totalSpent\"\nFROM customers c LEFT JOIN bookings b ON b.customer_id=c.id\nLEFT JOIN payments p ON p.booking_id=b.id\nGROUP BY c.id ORDER BY c.created_at DESC\n");
    }

    public Map<String, Object> customer(int id) {
        List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT id, user_id AS \"userId\", full_name AS \"fullName\", email, phone, country,\n       created_at AS \"createdAt\"\nFROM customers WHERE id=?\n", new Object[]{id});
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> customerByUser(int userId) {
        List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT id, user_id AS \"userId\", full_name AS \"fullName\", email, phone, country\nFROM customers WHERE user_id=?\n", new Object[]{userId});
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** An administrator keeps the same login while owning personal bookings and reviews. */
    public Map<String, Object> ensureForUser(com.wildtrail.security.SessionUser user) {
        // Serializing on the account prevents concurrent requests from creating two profiles.
        jdbc.queryForObject("SELECT id FROM users WHERE id=? FOR UPDATE", Integer.class, user.uid());
        Map<String, Object> profile = customerByUser(user.uid());
        if (profile != null) return profile;
        Map<String, Object> guest = customerByEmail(user.email());
        if (guest != null) {
            if (guest.get("userId") != null) {
                throw com.wildtrail.exception.ApiException.conflict("This email is linked to another customer account");
            }
            jdbc.update("UPDATE customers SET user_id=?,full_name=? WHERE id=?", user.uid(), user.name(), guest.get("id"));
        } else {
            createGuestCustomer(user.name(), user.email(), "", "", user.uid());
        }
        return customerByUser(user.uid());
    }

    public Map<String, Object> customerByEmail(String email) {
        List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT id, user_id AS \"userId\", full_name AS \"fullName\", email, phone, country\nFROM customers WHERE lower(email)=lower(?)\n", new Object[]{email});
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> updateCustomer(int id, Map<String, Object> body) {
        var current=customer(id);
        if(current!=null && current.get("userId")!=null)jdbc.update("UPDATE users SET full_name=?,email=? WHERE id=?",body.get("fullName"),body.get("email"),current.get("userId"));
        int changed = this.jdbc.update("UPDATE customers SET full_name=?, email=?, phone=NULLIF(?, ''), country=NULLIF(?, '')\nWHERE id=?\n", new Object[]{body.get("fullName"), body.get("email"), body.get("phone"), body.get("country"), id});
        return changed == 0 ? null : this.customer(id);
    }

    public boolean deleteCustomer(int id) {
        var current=customer(id);if(current==null)return false;
        if(jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE customer_id=?",Integer.class,id)>0)throw com.wildtrail.exception.ApiException.conflict("Customers with booking records cannot be deleted");
        jdbc.update("DELETE FROM customers WHERE id=?",id);
        if(current.get("userId")!=null)jdbc.update("DELETE FROM users WHERE id=? AND role='ROLE_CUSTOMER'",current.get("userId"));
        return true;
    }

    public boolean hasCompletedBooking(int id){return jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE customer_id=? AND status='COMPLETED'",Integer.class,id)>0;}

    public int createGuestCustomer(String name, String email, String phone, String country, Integer userId) {
        Integer id = JdbcInsert.id(this.jdbc, "INSERT INTO customers(full_name, email, phone, country, user_id)\nVALUES (?, ?, NULLIF(?, ''), NULLIF(?, ''), ?) RETURNING id\n", new Object[]{name, email.toLowerCase(), phone, country, userId});
        return id;
    }

    public void updateCustomerContact(int id, String phone, String country) {
        this.jdbc.update("UPDATE customers SET phone=?, country=COALESCE(NULLIF(?, ''), country) WHERE id=?", new Object[]{phone, country, id});
    }

}

