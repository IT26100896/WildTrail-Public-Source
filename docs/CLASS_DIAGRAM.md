# One integrated class diagram

This view connects the six business components in one system. It shows the principal implemented model inheritance and service/repository dependencies; the [complete project map](SYSTEM_MAP.md) links every Java class and website page. The `BookingService` dependencies show why package, resource, and payment changes need cross-module review.

```mermaid
classDiagram
  User <|-- Customer
  User <|-- Admin
  SafariPackage <|-- MorningSafari
  SafariPackage <|-- EveningSafari
  SafariPackage <|-- HalfDaySafari
  SafariPackage <|-- FullDaySafari
  SafariPackage <|-- PrivateSafari
  SafariPackage <|-- FamilySafari
  SafariPackage <|-- PhotographySafari
  Vehicle <|-- SafariJeep
  SafariJeep <|-- HiluxSafariJeep
  Vehicle <|-- LuxuryJeep
  SafariJeep <|-- FamilySafariJeep
  Guide <|-- NaturalistGuide
  Review <|-- VerifiedReview
  PaymentProcessor <|.. Payment
  Payment <|-- CashPayment
  Payment <|-- CardPayment
  Payment <|-- DemoCardPayment
  PaymentFactory ..> Payment
  AuthService --> User
  CustomerService --> Customer
  PackageService --> SafariPackage
  VehicleService --> Vehicle
  BookingService --> Booking
  BookingService --> PackageService
  BookingService --> VehicleService
  BookingService --> JdbcBookingRepository
  BookingService --> JdbcCustomerRepository
  BookingService --> JdbcPaymentRepository
  Booking ..> SafariPackage : quote
  Booking ..> Vehicle : quote
  PaymentService --> JdbcBookingRepository
  PaymentService --> JdbcPaymentRepository
  GuideController --> FileGuideRepository
  FileGuideRepository --> Guide
  ReviewService --> Review
  ReviewService --> JdbcReviewRepository
  ReviewService --> JdbcCustomerRepository
  ReviewController --> ReviewService : publication and staff replies
  MyReviewsController --> ReviewService : own history and deletion
  ProfileController --> JdbcCustomerRepository
  class SafariPackage {
    <<abstract>>
    -double basePrice
    -int maxGuests
    +calculatePrice(int guests) double
    +validateGuests(int guests) void
  }
  class Vehicle {
    <<abstract>>
    -int capacity
    -boolean available
    +calculateCost(int hours) double
    +ensureBookableFor(int guests) void
  }
  class PaymentProcessor {
    <<interface>>
    +process() PaymentResult
  }
  class Payment {
    <<abstract>>
    -double amount
  }
  class Booking {
    -String status
    +transitionTo(String next) void
    +validateDate(String date) String
    +quote(SafariPackage p, Vehicle v, int guests) Map
  }
```

The diagram can be rendered by Mermaid-capable Markdown viewers. Customer ownership, booking/payment records, and guide assignments are linked through the repositories and SQL schema; guide details are handled by `FileGuideRepository` in `resources.txt`. Data ownership is explained in [the technical overview](TECHNICAL_OVERVIEW.md).

The domain diagram focuses on Java behavior rather than every SQL field. Booking persistence additionally stores pickup place and the original safari/vehicle price split. Vehicle persistence stores registration and private owner metadata; administrator-only response mapping keeps owner contact/address/ID out of public vehicle data. Payment history joins the booking with its recorded collection date. Review replies are stored in SQL and exposed through `ReviewService`; these additions do not introduce separate domain subclasses. See [current feature behavior and source links](ACCOUNT_BOOKING_REVIEWS.md).
