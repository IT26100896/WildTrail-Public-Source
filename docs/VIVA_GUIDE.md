# Viva preparation

## Core demonstration flow

1. Admin sign in (private local credentials). Customers cannot open protected admin APIs; signed out admin pages redirect to login.
2. Customer register, booking date/package/vehicle choose, cash confirmation. Payment initially PENDING; booking is not a paid receipt.
3. My bookings → change guests; server reprices both booking and pending payment. Another customer cannot edit this booking.
4. Admin mark actual cash paid → booking CONFIRMED. Future/unpaid safaris cannot be completed. Customer cancellation needs 24 hours notice. Refund only after actual cash returned.
5. Guide register → `resources.txt` read after restart → assign active booking. One guide per day; overlap blocks conflicting assignment/date edit.
6. Review submit → admin approve → own edit returns to moderation → delete.
7. Contact inquiry → admin inbox. File reports Save → Read, with password hashes absent.

## OOP explanation

- Encapsulation: private validated fields in Booking, User, Guide, Review, SafariPackage and Vehicle. No direct field mutation from UI.
- Abstraction: abstract SafariPackage / Vehicle / Payment; PaymentProcessor interface. UI does not calculate authoritative prices or SQL operations.
- Inheritance: User → Customer/Admin; SafariPackage → seven safari types; Vehicle → Jeep subclasses; Guide → NaturalistGuide; Review → VerifiedReview.
- Polymorphism: PackageService creates actual subtype; `calculatePrice()` dispatches on that subtype. LuxuryJeep adds its surcharge; CashPayment returns pending, CardPayment deliberately rejects capture because no gateway is configured. Guide/review labels dispatch on subtype.
- Composition/dependency injection: controllers receive services; services receive repositories; BookingService coordinates package, vehicle, customer and payment collaborators.
- Transactions: booking and payment insert commit together; SQL row locking prevents concurrent double booking. Guide scheduling uses a database lock held to transaction commit.
- Files: UTF-8 JSON text for guides; atomic replacement plus fsync; unreadable originals are preserved. Snapshot reports are secondary copies, not authoritative SQL records. Audit uses append.

## Answer accurately

`resources.txt` handles guide file CRUD; `reports/*.txt` contains snapshots for SQL-backed modules. Bookings are stored in H2. Cash bookings remain pending until staff record payment. Card checkout is a campus demonstration and does not charge money. The app does not send outbound email or verify guide licences against a government service.

Explain why history-bearing customers/used packages/vehicles/guides and paid records cannot be casually deleted. Explain terminal booking states, Colombo date/time and error/status codes. Be ready to show source and tests; names of the four OOP concepts alone are insufficient.

## Before the viva

Run Java tests, lint, build, and typecheck; read the QA limits. Each teammate should be ready to explain their module and changes. Prepare a disposable database for demo records, and keep local credential files out of screenshots and Git.
