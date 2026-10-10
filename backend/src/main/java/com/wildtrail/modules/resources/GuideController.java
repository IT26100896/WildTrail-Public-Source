package com.wildtrail.modules.resources;
import com.wildtrail.util.RequestValues;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/guides") @PreAuthorize("hasRole('ADMIN')")
public class GuideController {
    private final FileGuideRepository repository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    public GuideController(FileGuideRepository repository,org.springframework.jdbc.core.JdbcTemplate jdbc){this.repository=repository;this.jdbc=jdbc;}
    @GetMapping public List<Guide> all(){return repository.findAll();}
    @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Guide create(@RequestBody Map<String,Object> body){return repository.save(0,parse(body));}
    @PutMapping("/{id}") @org.springframework.transaction.annotation.Transactional public synchronized Guide update(@PathVariable int id,@RequestBody Map<String,Object> body){lockSchedule();Guide guide=parse(body);if(!guide.isAvailable() && activeAssignments(id)>0)throw com.wildtrail.exception.ApiException.conflict("Reassign active safaris before marking the guide unavailable");return repository.save(id,guide);}
    @DeleteMapping("/{id}") @org.springframework.transaction.annotation.Transactional public synchronized Map<String,Boolean> remove(@PathVariable int id){lockSchedule();if(jdbc.queryForObject("SELECT COUNT(*) FROM booking_guide_assignments WHERE guide_id=?",Integer.class,id)>0)throw com.wildtrail.exception.ApiException.conflict("Guide has assignment history. Mark inactive instead.");repository.delete(id); return Map.of("ok",true);}
    @GetMapping("/assignments")public List<Map<String,Object>> assignments(){return jdbc.queryForList("SELECT booking_id AS \"bookingId\",guide_id AS \"guideId\" FROM booking_guide_assignments");}
    @PostMapping("/assignments") @org.springframework.transaction.annotation.Transactional
    public synchronized Map<String,Boolean> assign(@RequestBody Map<String,Object> body){
        jdbc.queryForObject("SELECT id FROM guide_schedule_lock WHERE id=1 FOR UPDATE",Integer.class);
        int bookingId=RequestValues.integer(body,"bookingId",1,Integer.MAX_VALUE),guideId=RequestValues.integer(body,"guideId",1,Integer.MAX_VALUE);
        Guide guide=repository.findAll().stream().filter(g->g.getId()==guideId).findFirst().orElseThrow(()->com.wildtrail.exception.ApiException.notFound("Guide was not found"));
        if(!guide.isAvailable())throw com.wildtrail.exception.ApiException.conflict("Guide is unavailable");
        var rows=jdbc.queryForList("SELECT safari_date,status FROM bookings WHERE id=? FOR UPDATE",bookingId);
        if(rows.isEmpty())throw com.wildtrail.exception.ApiException.notFound("Booking was not found");
        var booking=rows.get(0);
        if(!Set.of("PENDING","CONFIRMED").contains(booking.get("status")))throw com.wildtrail.exception.ApiException.conflict("Only active bookings can be assigned");
        if(jdbc.queryForObject("SELECT COUNT(*) FROM booking_guide_assignments a JOIN bookings b ON b.id=a.booking_id WHERE a.guide_id=? AND b.safari_date=? AND b.status<>'CANCELLED' AND b.id<>?",Integer.class,guideId,booking.get("safari_date"),bookingId)>0)throw com.wildtrail.exception.ApiException.conflict("Guide already has a safari that day");
        if(jdbc.update("UPDATE booking_guide_assignments SET guide_id=? WHERE booking_id=?",guideId,bookingId)==0)jdbc.update("INSERT INTO booking_guide_assignments(booking_id,guide_id) VALUES (?,?)",bookingId,guideId);
        return Map.of("ok",true);
    }
    private void lockSchedule(){jdbc.queryForObject("SELECT id FROM guide_schedule_lock WHERE id=1 FOR UPDATE",Integer.class);}
    private int activeAssignments(int id){return jdbc.queryForObject("SELECT COUNT(*) FROM booking_guide_assignments a JOIN bookings b ON b.id=a.booking_id WHERE a.guide_id=? AND b.status IN ('PENDING','CONFIRMED')",Integer.class,id);}
    private Guide parse(Map<String,Object> body){return Guide.create(0,RequestValues.text(body,"fullName",120),RequestValues.text(body,"phone",40),RequestValues.text(body,"licenseNumber",60),RequestValues.text(body,"specialty",20),RequestValues.bool(body,"available",true));}
}
