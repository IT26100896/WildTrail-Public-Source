package com.wildtrail;

import com.fasterxml.jackson.databind.*;
import com.wildtrail.modules.resources.FileGuideRepository;
import jakarta.servlet.http.Cookie;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:wildtrail_qa;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","APP_ORIGINS=http://localhost:3000,http://127.0.0.1:3000,https://campus.example.test","SESSION_SECRET=integration-only-key-not-for-deployment-123456789","ADMIN_EMAIL=qa-admin@example.test","ADMIN_PASSWORD=IntegrationOnly!123"})
@AutoConfigureMockMvc
class ApiIntegrationTest {
    static Path directory;
    static {try {directory=Files.createTempDirectory("wildtrail-qa-");}catch(Exception e){throw new RuntimeException(e);}}
    @DynamicPropertySource static void config(DynamicPropertyRegistry r){r.add("wildtrail.data-dir",()->directory.toString());}
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired FileGuideRepository guides;
    Cookie admin,customer;
    String email;
    String date=LocalDate.now(ZoneId.of("Asia/Colombo")).plusDays(10).toString();

    @BeforeEach void prepare() throws Exception {
        jdbc.update("DELETE FROM payments"); jdbc.update("DELETE FROM bookings");jdbc.update("DELETE FROM reviews");
        jdbc.update("DELETE FROM customers");jdbc.update("DELETE FROM users WHERE role='ROLE_CUSTOMER'");
        email="qa-"+UUID.randomUUID()+"@example.test";
        admin=login("/api/auth/admin-login","qa-admin@example.test","IntegrationOnly!123","wt_admin_session");
        customer=register(email);
    }
    Cookie register(String mail) throws Exception {
        var result=mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email",mail,"password","CustomerQA!123","fullName","QA Customer","phone","+94771234567")))).andExpect(status().isCreated()).andReturn();
        return cookie(result,"wt_client_session");
    }
    Cookie login(String path,String mail,String password,String name) throws Exception {
        return cookie(mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email",mail,"password",password)))).andExpect(status().isOk()).andReturn(),name);
    }
    Cookie cookie(MvcResult result,String name){String header=result.getResponse().getHeader("Set-Cookie");assertNotNull(header);String token=header.substring(header.indexOf('=')+1,header.indexOf(';'));return new Cookie(name,token);}
    Map<String,Object> request(){return new HashMap<>(Map.of("packageId",1,"vehicleId",1,"safariDate",date,"timeSlot","05:30","guests",2,"paymentMethod","CASH","fullName","QA Customer","email",email,"phone","+94771234567"));}
    JsonNode booking() throws Exception{return json(mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request()))).andExpect(status().isCreated()).andReturn());}
    JsonNode json(MvcResult r) throws Exception{return mapper.readTree(r.getResponse().getContentAsString());}
    void patchPayment(int id,String next,int expected) throws Exception {mvc.perform(patch("/api/payments/"+id).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("status",next)))).andExpect(status().is(expected));}
    int paymentId(int bookingId){return jdbc.queryForObject("SELECT id FROM payments WHERE booking_id=?",Integer.class,bookingId);}

    @Test void vehicleChangesRespectFutureBookingCapacityAndAvailability() throws Exception {
        booking();
        JsonNode vehicles = json(mvc.perform(get("/api/vehicles?all=1").cookie(admin))
                .andExpect(status().isOk()).andReturn());
        JsonNode vehicle = null;
        for (JsonNode candidate : vehicles) {
            if (candidate.get("id").asInt() == 1) {
                vehicle = candidate;
                break;
            }
        }
        assertNotNull(vehicle);
        Map<String, Object> update = mapper.convertValue(vehicle,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

        update.put("available", false);
        mvc.perform(put("/api/vehicles/1").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(update)))
                .andExpect(status().isConflict());

        update.put("available", true);
        update.put("capacity", 1);
        mvc.perform(put("/api/vehicles/1").cookie(admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(update)))
                .andExpect(status().isConflict());

        Map<String, Object> saved = jdbc.queryForMap(
                "SELECT capacity, available FROM vehicles WHERE id = 1");
        assertTrue((Boolean) saved.get("available"));
        assertTrue(((Number) saved.get("capacity")).intValue() >= 2);
    }

    @Test void customerTotalSpentCountsCollectedPaymentsOnly() throws Exception {
        var created = booking();
        int bookingId = created.get("id").asInt();
        var before = json(mvc.perform(get("/api/customers").cookie(admin)).andExpect(status().isOk()).andReturn());
        assertEquals(1, before.get(0).get("bookingCount").asInt());
        assertEquals(0, before.get(0).get("totalSpent").asDouble());
        patchPayment(paymentId(bookingId), "PAID", 200);
        var paid = json(mvc.perform(get("/api/customers").cookie(admin)).andExpect(status().isOk()).andReturn());
        assertEquals(created.get("totalAmount").asDouble(), paid.get(0).get("totalSpent").asDouble());
        patchPayment(paymentId(bookingId), "REFUNDED", 200);
        var refunded = json(mvc.perform(get("/api/customers").cookie(admin)).andExpect(status().isOk()).andReturn());
        assertEquals(0, refunded.get(0).get("totalSpent").asDouble());
    }

    @Test void configuredPublicOriginWorksThroughBothCorsAndMutationGuard() throws Exception {
        mvc.perform(post("/api/auth/login").header("Origin","https://campus.example.test").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email","qa-admin@example.test","password","IntegrationOnly!123"))))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","https://campus.example.test"));
        mvc.perform(post("/api/auth/logout").header("Origin","https://unconfigured.example.test")).andExpect(status().isForbidden());
    }
    @Test void unifiedLoginRoutesByStoredRoleAndLogoutClearsBothSessions() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email","qa-admin@example.test","password","IntegrationOnly!123")))).andExpect(status().isOk()).andExpect(jsonPath("$.redirect").value("/admin/dashboard"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email",email,"password","CustomerQA!123","role","ROLE_ADMIN")))).andExpect(status().isOk()).andExpect(jsonPath("$.redirect").value("/client/my-bookings"));
        var result=mvc.perform(post("/api/auth/logout").cookie(admin,customer)).andExpect(status().isOk()).andReturn();
        var cookies=result.getResponse().getHeaders("Set-Cookie");
        assertTrue(cookies.stream().anyMatch(c->c.startsWith("wt_admin_session=")&&c.contains("Max-Age=0")));
        assertTrue(cookies.stream().anyMatch(c->c.startsWith("wt_client_session=")&&c.contains("Max-Age=0")));
    }
    @Test void administratorProvisioningRequiresAdminAndCurrentPassword() throws Exception {
        String staffEmail="staff-"+UUID.randomUUID()+"@example.test";
        var body=new HashMap<String,Object>(Map.of("email",staffEmail,"fullName","QA Staff","staffTitle","Operations","password","NewStaffQA!12345","currentPassword","IntegrationOnly!123"));
        String payload=mapper.writeValueAsString(body);
        mvc.perform(get("/api/admin/accounts")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/accounts").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isForbidden());
        body.put("currentPassword","incorrect");mvc.perform(post("/api/admin/accounts").cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isUnauthorized());
        body.put("currentPassword","IntegrationOnly!123");body.put("password","short");mvc.perform(post("/api/admin/accounts").cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());
        body.put("password","NewStaffQA!12345");
        try {
            mvc.perform(post("/api/admin/accounts").cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.email").value(staffEmail));
            mvc.perform(post("/api/admin/accounts").cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isConflict());
            mvc.perform(get("/api/admin/accounts").cookie(admin)).andExpect(status().isOk()).andExpect(jsonPath("$[0].password_hash").doesNotExist());
            login("/api/auth/login",staffEmail,"NewStaffQA!12345","wt_admin_session");
        } finally {jdbc.update("DELETE FROM admins WHERE user_id IN (SELECT id FROM users WHERE email=?)",staffEmail);jdbc.update("DELETE FROM users WHERE email=?",staffEmail);}
    }
    @Test void adminRoleCannotBeForgedOrReachedByCustomer() throws Exception {
        mvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/dashboard").cookie(customer)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/dashboard").cookie(new Cookie("wt_admin_session",customer.getValue()))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/dashboard").cookie(new Cookie("wt_admin_session",admin.getValue()+"x"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/dashboard").cookie(admin)).andExpect(status().isOk());
    }
    @Test void failedLoginDuplicateRegisterAndMalformedBody() throws Exception {
        mvc.perform(post("/api/auth/admin-login").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email","qa-admin@example.test","password","wrong")))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("email",email,"fullName","Duplicate","password","CustomerQA!123")))).andExpect(status().isConflict());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{" )).andExpect(status().isBadRequest());
    }
    @Test void unsafeBrowserOriginRejected() throws Exception {
        mvc.perform(post("/api/bookings").header("Origin","https://attacker.example").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request()))).andExpect(status().isForbidden());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM bookings",Integer.class));
    }
    @Test void normalCashBookingAndReceiptOwnership() throws Exception {
        JsonNode b=booking();assertEquals("PENDING",b.get("status").asText());assertEquals("PENDING",b.get("paymentStatus").asText());assertEquals(145,b.get("totalAmount").asDouble());
        mvc.perform(get("/api/bookings/mine").cookie(customer)).andExpect(status().isOk()).andExpect(jsonPath("$[0].reference").value(b.get("reference").asText()));
        int id=b.get("id").asInt();mvc.perform(get("/api/bookings/"+id+"/receipt").cookie(customer)).andExpect(status().isOk());
        Cookie other=register("other-"+UUID.randomUUID()+"@example.test");mvc.perform(get("/api/bookings/"+id+"/receipt").cookie(other)).andExpect(status().isForbidden());
        mvc.perform(get("/api/bookings/"+id+"/receipt").cookie(admin)).andExpect(status().isOk());
    }
    @Test void validateCapacityDatesEmailAndUnknownSlotsWithoutSideEffects() throws Exception {
        for(var entry:List.of(Map.entry("guests",99),Map.entry("safariDate","2026-02-30"),Map.entry("timeSlot","99:99"),Map.entry("safariDate",LocalDate.now().minusDays(1).toString()),Map.entry("guests",1.5))){var body=request();body.put(entry.getKey(),entry.getValue());mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());}
        var body=request();body.put("email","other@example.test");mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isForbidden());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM bookings",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM payments",Integer.class));
    }
    @Test void noFakeCardCapture() throws Exception {
        var body=request();body.put("paymentMethod","CARD");mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM payments",Integer.class));
    }
    @Test void overlappingDeparturesConflictAndCancelReleasesResource() throws Exception {
        var first=booking();var body=request();body.put("timeSlot","06:00");mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isConflict());
        var availability=json(mvc.perform(get("/api/bookings/quote?availability=1&packageId=1&date="+date+"&slot=06:00")).andExpect(status().isOk()).andReturn());assertFalse(availability.get("availableVehicleIds").toString().matches(".*\\b1\\b.*"));
        mvc.perform(post("/api/bookings/mine").cookie(customer).contentType(MediaType.APPLICATION_JSON).content("{\"id\":"+first.get("id").asInt()+"}")).andExpect(status().isOk()).andExpect(jsonPath("$.paymentStatus").value("FAILED"));
        mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isCreated());
    }
    @Test void availabilityOnlyAcceptsBookableFutureDepartures() throws Exception {
        String today=LocalDate.now(ZoneId.of("Asia/Colombo")).toString();
        mvc.perform(get("/api/bookings/quote?availability=1&packageId=1&date="+today+"&slot=05:30")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/bookings/quote?availability=1&packageId=1&date=2026-02-30&slot=05:30")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/bookings/quote?availability=1&packageId=1&date="+date+"&slot=05:30")).andExpect(status().isOk());
    }
    @Test void cancelledBookingCannotBeCancelledAgain() throws Exception {
        int id=booking().get("id").asInt();
        String path="/api/bookings/mine";
        String body="{\"id\":"+id+"}";
        mvc.perform(post(path).cookie(customer).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mvc.perform(post(path).cookie(customer).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
        assertEquals("CANCELLED",jdbc.queryForObject("SELECT status FROM bookings WHERE id=?",String.class,id));
    }
    @Test void concurrentBookingsAllowExactlyOneReservation() throws Exception {
        var pool=Executors.newFixedThreadPool(2);var latch=new CountDownLatch(1);String body=mapper.writeValueAsString(request());
        Callable<Integer> submit=()->{latch.await();return mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(body)).andReturn().getResponse().getStatus();};
        var a=pool.submit(submit);var b=pool.submit(submit);latch.countDown();var results=new ArrayList<>(List.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS)));pool.shutdownNow();Collections.sort(results);assertEquals(List.of(201,409),results);assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM bookings",Integer.class));
    }
    @Test void editBookingRepricesPendingPaymentAndRejectsOtherOwner() throws Exception {
        var b=booking();int id=b.get("id").asInt();var change=Map.of("safariDate",date,"timeSlot","06:00","guests",3,"specialRequests","Updated pickup");
        mvc.perform(put("/api/bookings/mine/"+id).cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(change))).andExpect(status().isOk()).andExpect(jsonPath("$.totalAmount").value(200));
        assertEquals(200,jdbc.queryForObject("SELECT amount FROM payments WHERE booking_id=?",Double.class,id));
        var other=register("another-"+UUID.randomUUID()+"@example.test");mvc.perform(put("/api/bookings/mine/"+id).cookie(other).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(change))).andExpect(status().isForbidden());
    }
    @Test void cashPaymentConfirmationCancellationAndRefundAreConsistent() throws Exception {
        int id=booking().get("id").asInt(),pay=paymentId(id);patchPayment(pay,"REFUNDED",409);patchPayment(pay,"PAID",200);
        assertEquals("CONFIRMED",jdbc.queryForObject("SELECT status FROM bookings WHERE id=?",String.class,id));
        mvc.perform(post("/api/bookings/mine").cookie(customer).contentType(MediaType.APPLICATION_JSON).content("{\"id\":"+id+"}")).andExpect(status().isOk()).andExpect(jsonPath("$.paymentStatus").value("PAID"));
        mvc.perform(delete("/api/bookings/"+id).cookie(admin)).andExpect(status().isConflict());
        patchPayment(pay,"REFUNDED",200);patchPayment(pay,"PAID",409);
        mvc.perform(delete("/api/bookings/"+id).cookie(admin)).andExpect(status().isOk());
    }
    @Test void cannotCompleteFutureOrUnpaidBooking() throws Exception {
        int id=booking().get("id").asInt();mvc.perform(patch("/api/bookings/"+id).cookie(admin).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}")).andExpect(status().isBadRequest());
        patchPayment(paymentId(id),"PAID",200);mvc.perform(patch("/api/bookings/"+id).cookie(admin).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"COMPLETED\"}")).andExpect(status().isConflict());
    }
    @Test void profileUpdateSynchronizesIdentityAndDeletionRevokesCookie() throws Exception {
        String changed="changed-"+UUID.randomUUID()+"@example.test";
        mvc.perform(put("/api/profile").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("fullName","Changed Name","email",changed,"phone","+94770001111","country","Sri Lanka")))).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").cookie(customer)).andExpect(jsonPath("$.email").value(changed));
        mvc.perform(delete("/api/profile").cookie(customer)).andExpect(status().isOk());mvc.perform(get("/api/profile").cookie(customer)).andExpect(status().isUnauthorized());
    }
    @Test void reviewLifecycleModerationAndOwnership() throws Exception {
        mvc.perform(post("/api/reviews").cookie(customer).contentType(MediaType.APPLICATION_JSON).content("{\"authorName\":\"Spoofed\",\"rating\":5,\"comment\":\"Excellent safari\"}")).andExpect(status().isCreated());
        int id=jdbc.queryForObject("SELECT MAX(id) FROM reviews",Integer.class);assertEquals("QA Customer",jdbc.queryForObject("SELECT author_name FROM reviews WHERE id=?",String.class,id));
        mvc.perform(get("/api/reviews")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(patch("/api/reviews/"+id).cookie(admin).contentType(MediaType.APPLICATION_JSON).content("{\"approved\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/reviews")).andExpect(jsonPath("$.length()").value(1));
        Cookie other=register("review-other-"+UUID.randomUUID()+"@example.test");mvc.perform(delete("/api/reviews/mine/"+id).cookie(other)).andExpect(status().isNotFound());
        mvc.perform(put("/api/reviews/mine/"+id).cookie(customer).contentType(MediaType.APPLICATION_JSON).content("{\"rating\":4,\"comment\":\"Edited comment\"}")).andExpect(status().isOk());mvc.perform(get("/api/reviews")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(delete("/api/reviews/mine/"+id).cookie(customer)).andExpect(status().isOk());
    }
    @Test void guideCrudActuallySurvivesRepositoryRecreation() throws Exception {
        String licence="QA-"+UUID.randomUUID();var body=new HashMap<String,Object>(Map.of("fullName","Test Naturalist","phone","+94771112222","licenseNumber",licence,"specialty","NATURALIST","available",true));
        var saved=json(mvc.perform(post("/api/guides").cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isCreated()).andReturn());int id=saved.get("id").asInt();assertTrue(Files.exists(directory.resolve("resources.txt")));
        var reopened=new FileGuideRepository(mapper,directory.toString());assertTrue(reopened.findAll().stream().anyMatch(g->g.getId()==id && g.getAssignmentRole().contains("naturalist")));
        body.put("available",false);mvc.perform(put("/api/guides/"+id).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isOk());
        mvc.perform(delete("/api/guides/"+id).cookie(admin)).andExpect(status().isOk());assertTrue(reopened.findAll().stream().noneMatch(g->g.getId()==id));
    }
    @Test void fileSnapshotsExcludePasswordsAndValidateModulePaths() throws Exception {
        mvc.perform(post("/api/admin/files/users").cookie(admin)).andExpect(status().isOk());var read=mvc.perform(get("/api/admin/files/users").cookie(admin)).andExpect(status().isOk()).andReturn();assertFalse(read.getResponse().getContentAsString().contains("password"));
        mvc.perform(post("/api/admin/files/secrets").cookie(admin)).andExpect(status().isBadRequest());mvc.perform(get("/api/admin/files/users").cookie(customer)).andExpect(status().isForbidden());
    }
    @Test void catalogCrudReferencesAndInputValidation() throws Exception {
        var body=new HashMap<String,Object>(Map.of("name","QA Package","packageType","MORNING","destinationId",1,"durationHours",4,"startTime","05:30","maxGuests",6,"basePrice",25,"description","QA catalog package","imageUrl","https://example.test/safari.jpg"));
        body.put("startTime","99:99");mvc.perform(post("/api/packages").cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());body.put("startTime","05:30");
        var saved=json(mvc.perform(post("/api/packages").cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isCreated()).andReturn());int id=saved.get("id").asInt();body.put("basePrice",30);mvc.perform(put("/api/packages/"+id).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isOk());mvc.perform(delete("/api/packages/"+id).cookie(admin)).andExpect(status().isOk());
        booking();mvc.perform(delete("/api/packages/1").cookie(admin)).andExpect(status().isConflict());mvc.perform(delete("/api/vehicles/1").cookie(admin)).andExpect(status().isConflict());
    }
    @Test void dashboardRevenueMeansCollectedCash() throws Exception {
        int id=booking().get("id").asInt();mvc.perform(get("/api/admin/dashboard").cookie(admin)).andExpect(jsonPath("$.revenuePaid").value(0)).andExpect(jsonPath("$.revenueByPackage.length()").value(0));patchPayment(paymentId(id),"PAID",200);mvc.perform(get("/api/admin/dashboard").cookie(admin)).andExpect(jsonPath("$.revenuePaid").value(145)).andExpect(jsonPath("$.revenueByPackage[0].total").value(145));
    }

    @Test void contactPersistsWithoutExposingInbox() throws Exception {
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"QA Visitor\",\"email\":\"visitor@example.test\",\"message\":\"Need safari details\"}")).andExpect(status().isCreated());
        mvc.perform(get("/api/admin/messages").cookie(customer)).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/messages").cookie(admin)).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Need safari details")));
        mvc.perform(post("/api/contact").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"QA\",\"email\":\"invalid\",\"message\":\"test\"}")).andExpect(status().isBadRequest());
    }
    @Test void guideAssignmentRejectsDoubleScheduleAndInactiveGuide() throws Exception {
        var first=booking();var body=request();body.put("vehicleId",2);
        var second=json(mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isCreated()).andReturn());
        var guide=guides.save(0,com.wildtrail.modules.resources.Guide.create(0,"QA Guide","+94771234567","QA-"+UUID.randomUUID(),"NATURALIST",true));
        String path="/api/guides/assignments";
        mvc.perform(post(path).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("bookingId",first.get("id").asInt(),"guideId",guide.getId())))).andExpect(status().isOk());
        mvc.perform(post(path).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("bookingId",second.get("id").asInt(),"guideId",guide.getId())))).andExpect(status().isConflict());
        mvc.perform(delete("/api/guides/"+guide.getId()).cookie(admin)).andExpect(status().isConflict());
        mvc.perform(put("/api/guides/"+guide.getId()).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("fullName",guide.getFullName(),"phone",guide.getPhone(),"licenseNumber",guide.getLicenseNumber(),"specialty","NATURALIST","available",false)))).andExpect(status().isConflict());
        body.put("safariDate",LocalDate.parse(date).plusDays(1).toString());
        mvc.perform(put("/api/bookings/mine/"+second.get("id").asInt()).cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isOk());
        mvc.perform(post(path).cookie(admin).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("bookingId",second.get("id").asInt(),"guideId",guide.getId())))).andExpect(status().isOk());
        body.put("safariDate",date);
        mvc.perform(put("/api/bookings/mine/"+second.get("id").asInt()).cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isConflict());
    }
    @Test void corruptGuideFileNeverOverwritesOriginal() throws Exception {
        Path isolated=Files.createTempDirectory("wildtrail-corrupt-");Path data=isolated.resolve("resources.txt");Files.writeString(data,"broken original");
        var repository=new FileGuideRepository(mapper,isolated.toString());
        assertThrows(IllegalStateException.class,repository::findAll);
        assertThrows(IllegalStateException.class,()->repository.save(0,com.wildtrail.modules.resources.Guide.create(0,"Guide","+94771234567","LICENSE-QA","DRIVER",true)));
        assertEquals("broken original",Files.readString(data));
    }
    @Test void failedSignInsAreRateLimitedPerAddress() throws Exception {
        for(int i=0;i<10;i++)mvc.perform(post("/api/auth/login").with(req->{req.setRemoteAddr("192.0.2.77");return req;}).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"none@example.test\",\"password\":\"wrong-password\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").with(req->{req.setRemoteAddr("192.0.2.77");return req;}).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"none@example.test\",\"password\":\"wrong-password\"}")).andExpect(status().isTooManyRequests());
    }
    @Test void demoCardBookingRemainsPendingAndRejectsCardDetails() throws Exception {
        var body=request();body.put("paymentMethod","CARD");body.put("cardDemo",true);body.put("cardNumber","4242424242424242");
        mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isBadRequest());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM payments",Integer.class));
        body.remove("cardNumber");
        var b=json(mvc.perform(post("/api/bookings").cookie(customer).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andExpect(status().isCreated()).andReturn());
        assertEquals("PENDING",b.get("status").asText());assertEquals("PENDING",b.get("paymentStatus").asText());
        assertTrue(b.get("transactionRef").asText().startsWith("DEMO-"));
        assertNull(jdbc.queryForObject("SELECT paid_at FROM payments LIMIT 1",java.sql.Timestamp.class));
    }
}

