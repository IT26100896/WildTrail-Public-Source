# Complete WildTrail project map

**Group:** 26-MTR-Y1S2-WD-009 · **Course:** SE1020 Object Oriented Programming

This is one integrated application. The six owners below are responsible for explaining their business flow and relevant source; cross-module files remain shared. The tables inventory the current Java source and website pages, so a reader can open a class directly from GitHub.

## Request and data flow

```text
Customer / staff browser
    → Next.js page and component
    → /api request through the Next.js proxy
    → Spring Boot controller and session/role checks
    → Java service and validated domain model
    → JDBC repository and H2 SQL, or FileGuideRepository and resources.txt
```

A booking crosses components: customer identity (01), package and price (02), booking rules (03), vehicle and guide availability (04), and payment state (05). A review (06) belongs to a signed-in customer or administrator, appears immediately, and supports staff replies and deletion. The [class diagram](CLASS_DIAGRAM.md) shows the principal relationships in one view.

## Six component class inventories

### 01. Users — Mekhala B.G.R.S (IT26100894)

Registration, sessions, profiles, administrators, and customer records.

| Java role | Classes |
|---|---|
| Domain models / contracts | [Admin.java](../backend/src/main/java/com/wildtrail/modules/users/Admin.java), [Customer.java](../backend/src/main/java/com/wildtrail/modules/users/Customer.java), [User.java](../backend/src/main/java/com/wildtrail/modules/users/User.java) |
| Controllers | [AdminAccountController.java](../backend/src/main/java/com/wildtrail/modules/users/AdminAccountController.java), [AuthController.java](../backend/src/main/java/com/wildtrail/modules/users/AuthController.java), [CustomerController.java](../backend/src/main/java/com/wildtrail/modules/users/CustomerController.java), [ProfileController.java](../backend/src/main/java/com/wildtrail/modules/users/ProfileController.java) |
| Services / factories | [AuthService.java](../backend/src/main/java/com/wildtrail/modules/users/AuthService.java), [CustomerService.java](../backend/src/main/java/com/wildtrail/modules/users/CustomerService.java) |
| Repositories | [JdbcCustomerRepository.java](../backend/src/main/java/com/wildtrail/modules/users/JdbcCustomerRepository.java), [JdbcUserRepository.java](../backend/src/main/java/com/wildtrail/modules/users/JdbcUserRepository.java) |

Member-specific UI paths and viva steps: [README.md](../team/IT26100894-Mekhala-BGRS/README.md).

### 02. Safaris — Mihiran S.P.D (IT26100895)

Destinations, safari catalogue, package subtypes, and prices.

| Java role | Classes |
|---|---|
| Domain models / contracts | [EveningSafari.java](../backend/src/main/java/com/wildtrail/modules/safaris/EveningSafari.java), [FamilySafari.java](../backend/src/main/java/com/wildtrail/modules/safaris/FamilySafari.java), [FullDaySafari.java](../backend/src/main/java/com/wildtrail/modules/safaris/FullDaySafari.java), [HalfDaySafari.java](../backend/src/main/java/com/wildtrail/modules/safaris/HalfDaySafari.java), [MorningSafari.java](../backend/src/main/java/com/wildtrail/modules/safaris/MorningSafari.java), [PhotographySafari.java](../backend/src/main/java/com/wildtrail/modules/safaris/PhotographySafari.java), [PrivateSafari.java](../backend/src/main/java/com/wildtrail/modules/safaris/PrivateSafari.java), [SafariPackage.java](../backend/src/main/java/com/wildtrail/modules/safaris/SafariPackage.java) |
| Controllers | [PackageController.java](../backend/src/main/java/com/wildtrail/modules/safaris/PackageController.java) |
| Services / factories | [PackageService.java](../backend/src/main/java/com/wildtrail/modules/safaris/PackageService.java) |
| Repositories | [JdbcPackageRepository.java](../backend/src/main/java/com/wildtrail/modules/safaris/JdbcPackageRepository.java) |

Member-specific UI paths and viva steps: [README.md](../team/IT26100895-Mihiran-SPD/README.md).

### 03. Bookings — Nayanajith W.R.S.C (IT26100896)

Availability, reservations, changes, cancellation, and status rules.

| Java role | Classes |
|---|---|
| Domain models / contracts | [Booking.java](../backend/src/main/java/com/wildtrail/modules/bookings/Booking.java) |
| Controllers | [BookingController.java](../backend/src/main/java/com/wildtrail/modules/bookings/BookingController.java) |
| Services / factories | [BookingService.java](../backend/src/main/java/com/wildtrail/modules/bookings/BookingService.java) |
| Repositories | [JdbcBookingRepository.java](../backend/src/main/java/com/wildtrail/modules/bookings/JdbcBookingRepository.java) |

Member-specific UI paths and viva steps: [README.md](../team/IT26100896-Nayanajith-WRSC/README.md).

### 04. Resources — Senevirathna M.K.K.T (IT26100899)

Vehicles, guides, assignments, schedules, and guide file CRUD.

| Java role | Classes |
|---|---|
| Domain models / contracts | [FamilySafariJeep.java](../backend/src/main/java/com/wildtrail/modules/resources/FamilySafariJeep.java), [Guide.java](../backend/src/main/java/com/wildtrail/modules/resources/Guide.java), [HiluxSafariJeep.java](../backend/src/main/java/com/wildtrail/modules/resources/HiluxSafariJeep.java), [LuxuryJeep.java](../backend/src/main/java/com/wildtrail/modules/resources/LuxuryJeep.java), [NaturalistGuide.java](../backend/src/main/java/com/wildtrail/modules/resources/NaturalistGuide.java), [SafariJeep.java](../backend/src/main/java/com/wildtrail/modules/resources/SafariJeep.java), [Vehicle.java](../backend/src/main/java/com/wildtrail/modules/resources/Vehicle.java) |
| Controllers | [GuideController.java](../backend/src/main/java/com/wildtrail/modules/resources/GuideController.java), [TeamController.java](../backend/src/main/java/com/wildtrail/modules/resources/TeamController.java), [VehicleController.java](../backend/src/main/java/com/wildtrail/modules/resources/VehicleController.java) |
| Services / factories | [VehicleService.java](../backend/src/main/java/com/wildtrail/modules/resources/VehicleService.java) |
| Repositories | [FileGuideRepository.java](../backend/src/main/java/com/wildtrail/modules/resources/FileGuideRepository.java), [JdbcVehicleRepository.java](../backend/src/main/java/com/wildtrail/modules/resources/JdbcVehicleRepository.java) |

Member-specific UI paths and viva steps: [README.md](../team/IT26100899-Senevirathna-MKKT/README.md).

### 05. Payments — Lakshani W.G.C (IT26100900)

Pending payments, cash collection, refunds, and receipts.

| Java role | Classes |
|---|---|
| Domain models / contracts | [CardPayment.java](../backend/src/main/java/com/wildtrail/modules/payments/CardPayment.java), [CashPayment.java](../backend/src/main/java/com/wildtrail/modules/payments/CashPayment.java), [DemoCardPayment.java](../backend/src/main/java/com/wildtrail/modules/payments/DemoCardPayment.java), [Payment.java](../backend/src/main/java/com/wildtrail/modules/payments/Payment.java), [PaymentProcessor.java](../backend/src/main/java/com/wildtrail/modules/payments/PaymentProcessor.java), [PaymentResult.java](../backend/src/main/java/com/wildtrail/modules/payments/PaymentResult.java) |
| Controllers | [PaymentController.java](../backend/src/main/java/com/wildtrail/modules/payments/PaymentController.java), [ReceiptController.java](../backend/src/main/java/com/wildtrail/modules/payments/ReceiptController.java) |
| Services / factories | [PaymentFactory.java](../backend/src/main/java/com/wildtrail/modules/payments/PaymentFactory.java), [PaymentService.java](../backend/src/main/java/com/wildtrail/modules/payments/PaymentService.java) |
| Repositories | [JdbcPaymentRepository.java](../backend/src/main/java/com/wildtrail/modules/payments/JdbcPaymentRepository.java) |

Member-specific UI paths and viva steps: [README.md](../team/IT26100900-Lakshani-WGC/README.md).

### 06. Reviews — Gamage C.I.P (IT26100901)

Customer/admin reviews, immediate publication, staff replies, deletion, ratings, and contact feedback.

| Java role | Classes |
|---|---|
| Domain models / contracts | [Review.java](../backend/src/main/java/com/wildtrail/modules/reviews/Review.java), [VerifiedReview.java](../backend/src/main/java/com/wildtrail/modules/reviews/VerifiedReview.java) |
| Controllers | [MyReviewsController.java](../backend/src/main/java/com/wildtrail/modules/reviews/MyReviewsController.java), [ReviewController.java](../backend/src/main/java/com/wildtrail/modules/reviews/ReviewController.java) |
| Services / factories | [ReviewService.java](../backend/src/main/java/com/wildtrail/modules/reviews/ReviewService.java) |
| Repositories | [JdbcReviewRepository.java](../backend/src/main/java/com/wildtrail/modules/reviews/JdbcReviewRepository.java) |

Member-specific UI paths and viva steps: [README.md](../team/IT26100901-Gamage-CIP/README.md).

## Shared Java application and infrastructure

These classes support several components; they are not a seventh business module.

| Area | Source files |
|---|---|
| `config/` | [AdminBootstrap.java](../backend/src/main/java/com/wildtrail/config/AdminBootstrap.java), [DatabaseSeeder.java](../backend/src/main/java/com/wildtrail/config/DatabaseSeeder.java), [SecurityConfig.java](../backend/src/main/java/com/wildtrail/config/SecurityConfig.java), [WebConfig.java](../backend/src/main/java/com/wildtrail/config/WebConfig.java) |
| `controller/` | [AdminController.java](../backend/src/main/java/com/wildtrail/controller/AdminController.java), [ContactController.java](../backend/src/main/java/com/wildtrail/controller/ContactController.java), [FileReportController.java](../backend/src/main/java/com/wildtrail/controller/FileReportController.java), [HealthController.java](../backend/src/main/java/com/wildtrail/controller/HealthController.java), [InboxController.java](../backend/src/main/java/com/wildtrail/controller/InboxController.java), [SessionController.java](../backend/src/main/java/com/wildtrail/controller/SessionController.java) |
| `exception/` | [ApiException.java](../backend/src/main/java/com/wildtrail/exception/ApiException.java), [ApiExceptionHandler.java](../backend/src/main/java/com/wildtrail/exception/ApiExceptionHandler.java) |
| `repository/` | [JdbcInsert.java](../backend/src/main/java/com/wildtrail/repository/JdbcInsert.java) |
| `security/` | [AuditFilter.java](../backend/src/main/java/com/wildtrail/security/AuditFilter.java), [LoginThrottle.java](../backend/src/main/java/com/wildtrail/security/LoginThrottle.java), [OriginGuard.java](../backend/src/main/java/com/wildtrail/security/OriginGuard.java), [SessionAuthenticationFilter.java](../backend/src/main/java/com/wildtrail/security/SessionAuthenticationFilter.java), [SessionCookieService.java](../backend/src/main/java/com/wildtrail/security/SessionCookieService.java), [SessionUser.java](../backend/src/main/java/com/wildtrail/security/SessionUser.java) |
| `service/` | [AdminService.java](../backend/src/main/java/com/wildtrail/service/AdminService.java) |
| `util/` | [RequestValues.java](../backend/src/main/java/com/wildtrail/util/RequestValues.java) |
| Entry point | [WildTrailApplication.java](../backend/src/main/java/com/wildtrail/WildTrailApplication.java) |

## Website pages

Every website page below is part of the same Next.js application. A page can call more than one Java component.

| Area | Pages |
|---|---|
| `client/` | [page.tsx](../src/app/client/about/page.tsx), [page.tsx](../src/app/client/booking/page.tsx), [page.tsx](../src/app/client/contact/page.tsx), [page.tsx](../src/app/client/destinations/page.tsx), [page.tsx](../src/app/client/gallery/page.tsx), [page.tsx](../src/app/client/home/page.tsx), [page.tsx](../src/app/client/login/page.tsx), [page.tsx](../src/app/client/my-bookings/page.tsx), [page.tsx](../src/app/client/my-reviews/page.tsx), [page.tsx](../src/app/client/packages/page.tsx), [page.tsx](../src/app/client/page.tsx), [page.tsx](../src/app/client/profile/page.tsx), [page.tsx](../src/app/client/receipts/%5Bid%5D/page.tsx), [page.tsx](../src/app/client/register/page.tsx), [page.tsx](../src/app/client/reviews/page.tsx), [page.tsx](../src/app/client/safaris/page.tsx), [page.tsx](../src/app/client/vehicles/page.tsx) |
| `admin/` | [page.tsx](../src/app/admin/%28panel%29/accounts/page.tsx), [page.tsx](../src/app/admin/%28panel%29/bookings/page.tsx), [page.tsx](../src/app/admin/%28panel%29/customers/page.tsx), [page.tsx](../src/app/admin/%28panel%29/dashboard/page.tsx), [page.tsx](../src/app/admin/%28panel%29/files/page.tsx), [page.tsx](../src/app/admin/%28panel%29/guides/page.tsx), [page.tsx](../src/app/admin/%28panel%29/messages/page.tsx), [page.tsx](../src/app/admin/%28panel%29/packages/page.tsx), [page.tsx](../src/app/admin/%28panel%29/payments/page.tsx), [page.tsx](../src/app/admin/%28panel%29/reviews/page.tsx), [page.tsx](../src/app/admin/%28panel%29/vehicles/page.tsx), [page.tsx](../src/app/admin/login/page.tsx), [page.tsx](../src/app/admin/page.tsx) |
| Routing/layout | [layout.tsx](../src/app/admin/%28panel%29/layout.tsx), [layout.tsx](../src/app/admin/layout.tsx), [layout.tsx](../src/app/client/layout.tsx), [error.tsx](../src/app/error.tsx), [layout.tsx](../src/app/layout.tsx) |

## Shared website components and contracts

Shared files can contain functions from several modules. Ownership follows the function and related business rule, not the whole file.

| Area | Source files |
|---|---|
| `src/components/client/` | [AuthPanel.tsx](../src/components/client/AuthPanel.tsx), [BookingWizard.tsx](../src/components/client/BookingWizard.tsx), [ClientShell.tsx](../src/components/client/ClientShell.tsx), [Footer.tsx](../src/components/client/Footer.tsx), [Hero.tsx](../src/components/client/Hero.tsx), [MyReviews.tsx](../src/components/client/MyReviews.tsx), [Navbar.tsx](../src/components/client/Navbar.tsx), [PrintButton.tsx](../src/components/client/PrintButton.tsx), [ProfileForm.tsx](../src/components/client/ProfileForm.tsx), [PaymentSummary.tsx](../src/components/client/PaymentSummary.tsx), [ReviewReplies.tsx](../src/components/client/ReviewReplies.tsx), [QuickBooking.tsx](../src/components/client/QuickBooking.tsx), [RevealObserver.tsx](../src/components/client/RevealObserver.tsx), [cards.tsx](../src/components/client/cards.tsx), [forms.tsx](../src/components/client/forms.tsx), [interactive.tsx](../src/components/client/interactive.tsx), [sections.tsx](../src/components/client/sections.tsx) |
| `src/components/admin/` | [AdminAccountsManager.tsx](../src/components/admin/AdminAccountsManager.tsx), [AdminLoginForm.tsx](../src/components/admin/AdminLoginForm.tsx), [AdminSidebar.tsx](../src/components/admin/AdminSidebar.tsx), [managers.tsx](../src/components/admin/managers.tsx) |
| `src/lib/` | [api.ts](../src/lib/api.ts), [backend-api.ts](../src/lib/backend-api.ts), [content.ts](../src/lib/content.ts), [session.ts](../src/lib/session.ts) |
| `src/types/` | [domain.ts](../src/types/domain.ts) |

## Storage, setup, and verification

| Folder | Role |
|---|---|
| [`database/`](../database/) | SQL schema and sample catalogue. H2 is the verified local database for most entities. |
| `resources.txt` (created locally) | UTF-8 JSON guide records read and written by `FileGuideRepository`. It is local data, not a tracked shared password file. |
| [`scripts/`](../scripts/) | Local setup and start commands. `.local/` credentials and `backend/data/` databases are ignored by Git. |
| [`backend/src/test/`](../backend/src/test/) | Java tests for domain, API, repository, booking, payment, security, and other rules. |
| [`qa/`](../qa/) | Browser, authentication/navigation, and responsive checks. Browser QA mutates only a disposable database. |
| [`docs/`](./) | Final report, class diagram, QA evidence, and viva notes. |

## Cross-module dependencies to explain in the viva

1. **Users → bookings:** the authenticated customer owns the booking; staff access is role checked by the Java API.
2. **Safaris + resources → bookings:** package rules set price and guest limits; vehicle availability and capacity protect the reserved slot.
3. **Bookings → payments:** creating or changing a booking coordinates the related pending payment and price in a transaction.
4. **Customers + reviews:** a signed-in customer can manage their own review; reviews appear immediately and staff can reply or delete them.
5. **H2 + files:** SQL is authoritative for most records. Guide CRUD really reads/writes `resources.txt`; staff text reports are snapshots.

The [main README](../README.md) gives the overview, [workload guide](WORKLOAD_DISTRIBUTION.md) assigns responsibilities, and [viva guide](VIVA_GUIDE.md) gives the demonstration route. This map lists implementation locations; the Git history is the evidence for specific individual changes.
