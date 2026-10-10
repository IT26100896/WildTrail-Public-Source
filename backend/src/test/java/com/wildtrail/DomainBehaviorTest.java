package com.wildtrail;
import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.safaris.*;
import com.wildtrail.modules.resources.*;
import com.wildtrail.modules.payments.*;
import com.wildtrail.modules.bookings.Booking;
import com.wildtrail.modules.reviews.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DomainBehaviorTest {
    private SafariPackage item(String type) {
        return switch(type){
            case "MORNING"->new MorningSafari(1,"Test",1,"Yala",4,"05:30",8,100,"Description","",true);
            case "EVENING"->new EveningSafari(1,"Test",1,"Yala",4,"14:00",8,100,"Description","",true);
            case "HALF_DAY"->new HalfDaySafari(1,"Test",1,"Yala",5,"05:30",8,100,"Description","",true);
            case "FULL_DAY"->new FullDaySafari(1,"Test",1,"Yala",12,"05:30",8,100,"Description","",true);
            case "PRIVATE"->new PrivateSafari(1,"Test",1,"Yala",6,"05:30",8,100,"Description","",true);
            case "FAMILY"->new FamilySafari(1,"Test",1,"Yala",4,"14:00",8,100,"Description","",true);
            default->new PhotographySafari(1,"Test",1,"Yala",8,"05:30",8,100,"Description","",true);
        };
    }
    @ParameterizedTest @CsvSource({"MORNING,2,200","EVENING,2,200","HALF_DAY,2,200","FULL_DAY,3,300","FULL_DAY,4,360","PRIVATE,4,100","FAMILY,4,100","FAMILY,6,130","PHOTOGRAPHY,2,225"})
    void polymorphicPackagePricing(String type,int guests,double expected){assertEquals(expected,item(type).calculatePrice(guests));}
    @ParameterizedTest @CsvSource({"STANDARD,4,40","STANDARD,8,80","HILUX,4,40","HILUX,8,80","LUXURY,4,60","LUXURY,8,100","FAMILY,4,40","FAMILY,8,80"})
    void vehiclePricingAndCapacity(String type,int hours,double expected){
        Vehicle v=switch(type){case "HILUX"->new HiluxSafariJeep(1,"Test",6,true,List.of(),true,40,"");case "LUXURY"->new LuxuryJeep(1,"Test",6,true,List.of(),true,40,"");case "FAMILY"->new FamilySafariJeep(1,"Test",6,true,List.of(),true,40,"");default->new SafariJeep(1,"Test",6,true,List.of(),true,40,"");};
        assertEquals(expected,v.calculateCost(hours));assertThrows(ApiException.class,()->v.ensureBookableFor(7));
    }
    @Test void invalidGuestsAndCalendarDates(){assertThrows(ApiException.class,()->item("MORNING").calculatePrice(0));assertThrows(ApiException.class,()->Booking.validateDate("2027-02-30"));assertThrows(ApiException.class,()->Booking.validateSlot(item("FULL_DAY"),"14:00"));}
    @Test void terminalBookingCannotReopen(){Booking b=new Booking("PENDING");b.transitionTo("CONFIRMED");b.transitionTo("COMPLETED");assertThrows(ApiException.class,()->b.transitionTo("PENDING"));}
    @Test void cashNeverPretendsToBeCollected(){var cash=new CashPayment(100).process();assertEquals("PENDING",cash.status());assertNull(cash.paidAt());assertNull(cash.cardLast4());}
    @Test void cardCannotClaimGatewayCapture(){assertThrows(ApiException.class,()->new CardPayment(100,"Test","4242424242424242","12/35","123").process());}
    @Test void specializedGuideAndReviewContracts(){Guide driver=Guide.create(1,"Driver","+94771234567","LICENSE-1","DRIVER",true);Guide naturalist=Guide.create(2,"Guide","+94771234567","LICENSE-2","NATURALIST",true);assertNotEquals(driver.getAssignmentRole(),naturalist.getAssignmentRole());Review a=new Review(5,"Review"),b=new VerifiedReview(5,"Review");assertNotEquals(a.getType(),b.getType());assertThrows(ApiException.class,()->new Review(6,"Invalid"));}
    @org.junit.jupiter.api.Test void cardDemoNeverCapturesOrRetainsCardData() {
        var result=com.wildtrail.modules.payments.PaymentFactory.create("CARD",100,java.util.Map.of("cardDemo",true)).process();
        org.junit.jupiter.api.Assertions.assertEquals("PENDING",result.status());
        org.junit.jupiter.api.Assertions.assertNull(result.paidAt());
        org.junit.jupiter.api.Assertions.assertNull(result.cardLast4());
        org.junit.jupiter.api.Assertions.assertTrue(result.transactionRef().startsWith("DEMO-"));
        org.junit.jupiter.api.Assertions.assertThrows(com.wildtrail.exception.ApiException.class,()->com.wildtrail.modules.payments.PaymentFactory.create("CARD",100,java.util.Map.of("cardDemo",true,"cardNumber","4242424242424242")));
    }
}
