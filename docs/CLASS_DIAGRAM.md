# Class relationships

Focused diagram of implemented OOP relationships. Services/controllers/repositories not shown exhaustively.

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
  Vehicle <|-- HiluxSafariJeep
  Vehicle <|-- LuxuryJeep
  Vehicle <|-- FamilySafariJeep
  Guide <|-- NaturalistGuide
  Review <|-- VerifiedReview
  PaymentProcessor <|.. Payment
  Payment <|-- CashPayment
  Payment <|-- CardPayment
  PaymentFactory ..> Payment
  BookingService --> Booking
  BookingService --> PackageService
  BookingService --> VehicleService
  BookingService --> JdbcBookingRepository
  BookingService --> JdbcPaymentRepository
  GuideController --> FileGuideRepository
  FileGuideRepository --> Guide
  ReviewService --> Review
  ReviewService --> JdbcReviewRepository
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

The diagram can be rendered by Mermaid-capable Markdown viewers. SQL and file ownership are explained in [the technical overview](TECHNICAL_OVERVIEW.md).
