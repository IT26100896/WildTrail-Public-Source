# Viva preparation

## Core demonstration flow

1. Admin sign in (private local credentials) → View website → Admin account menu → Admin dashboard. The account name, email and role remain visible on the website. Customers cannot open protected admin APIs; signed out admin pages redirect to login.
2. Customer register, choose booking date/package/vehicle and pickup place, then cash confirmation. An administrator can also create a personal booking using the same website flow. Payment initially PENDING; booking is not a paid receipt.
3. My bookings → change guests; server reprices both booking and pending payment. Another customer cannot edit this booking.
4. Admin mark actual cash paid → booking CONFIRMED. My Payments and the receipt show the recorded payment date, amount, saved safari/vehicle costs, pickup place and registration number. Future/unpaid safaris cannot be completed. Customer cancellation needs 24 hours notice. Refund only after actual cash returned.
5. Guide register → `resources.txt` read after restart → assign active booking. One guide per day; overlap blocks conflicting assignment/date edit.
6. Review submit → immediate public display → administrator reply → owner/admin delete.
7. Vehicle create → enter registration and owner name/phone/address/ID → edit → reload → inspect private Owner details. Public vehicle responses omit owner data. Delete the disposable vehicle; vehicles linked to booking history stay protected.
8. Contact inquiry → admin inbox. File reports Save → Read, with password hashes absent.

## OOP explanation

- Encapsulation: private validated fields in Booking, User, Guide, Review, SafariPackage and Vehicle. No direct field mutation from UI.
- Abstraction: abstract SafariPackage / Vehicle / Payment; PaymentProcessor interface. UI does not calculate authoritative prices or SQL operations.
- Inheritance: User → Customer/Admin; SafariPackage → seven safari types; Vehicle → Jeep subclasses; Guide → NaturalistGuide; Review → VerifiedReview.
- Polymorphism: PackageService creates actual subtype; `calculatePrice()` dispatches on that subtype. LuxuryJeep adds its surcharge; CashPayment returns pending, and PaymentFactory selects DemoCardPayment for campus card checkout. CardPayment deliberately rejects real capture because no gateway is configured. Guide/review labels dispatch on subtype.
- Composition/dependency injection: controllers receive services; services receive repositories; BookingService coordinates package, vehicle, customer and payment collaborators.
- Transactions: booking and payment insert commit together; SQL row locking prevents concurrent double booking. Guide scheduling uses a database lock held to transaction commit.
- Files: UTF-8 JSON text for guides; atomic replacement plus fsync; unreadable originals are preserved. Snapshot reports are secondary copies, not authoritative SQL records. Audit uses append.

## Answer accurately

`resources.txt` handles guide file CRUD; `reports/*.txt` contains snapshots for SQL-backed modules. Bookings are stored in H2. Cash bookings remain pending until staff record payment. Card checkout is a campus demonstration and does not charge money. The app does not send outbound email or verify guide licences against a government service.

Explain why history-bearing customers/used packages/vehicles/guides and paid records cannot be casually deleted. Explain terminal booking states, Colombo date/time and error/status codes. Be ready to show source and tests; names of the four OOP concepts alone are insufficient.

## Questions about the latest features

| Question | Short answer |
|---|---|
| Can an administrator use the customer website? | Yes. The existing signed-in identity can own bookings, payments and reviews; staff management APIs still check the administrator role. |
| Why is a pending payment missing a payment date? | The date records actual collection. Creating a pending payment does not mean money was paid. |
| Why store safari and vehicle costs on the booking? | A later price change should not recalculate an old booking summary. Older records show Included in total when the split was not saved. |
| Who can read vehicle owner ID and address? | Administrators in the fleet view. Public and customer responses omit all four owner fields. |
| How are owner details removed? | Clear a field explicitly and save, or delete an unused vehicle. An omitted field is preserved for compatibility. |
| Who approves reviews? | Publication is immediate. Authors delete their own reviews, and administrators can reply or delete any review. |
| Where do review names and emails come from? | The authenticated account, not values supplied by the review form. Public responses omit the email. |
| Why do deleted sample reviews stay deleted after restart? | A stored migration marker prevents the one-time sample seed from running again. |

See [the feature guide](ACCOUNT_BOOKING_REVIEWS.md) for the linked implementation.

## Before the viva

Run Java tests, lint, build, and typecheck; read the QA limits. Each teammate should be ready to explain their module and changes. Prepare a disposable database for demo records, and keep local credential files out of screenshots and Git.
