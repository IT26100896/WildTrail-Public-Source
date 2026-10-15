# WildTrail technical overview

**Group:** 26-MTR-Y1S2-WD-009

**Course:** SE1020 Object Oriented Programming

## Purpose

WildTrail supports safari discovery, reservations, customer accounts, resource scheduling, payments, feedback, and staff operations. The website uses Next.js and TypeScript; the Java Spring Boot API owns authentication, validation, business rules, and persistence.

## Users and workflows

Visitors can browse destinations, packages, vehicles, and published reviews, and submit contact inquiries. Customers can register, sign in, manage profiles, create and view reservations, request eligible booking changes or cancellations, view payment history and receipts, and publish or delete their own reviews. Administrators can also use those personal website flows with their existing account, while the website menu shows their name, email and role. Staff can manage catalogue entries, resources, bookings, payments, customers, reviews, and contact messages.

## Architecture and data

Controllers delegate to services and domain objects, which use repositories for persistence. Java verifies signed HttpOnly sessions against current accounts. Public registration creates customer accounts only. Login clears the other role cookie, and logout clears both role cookies. An administrator can use View website and return through Admin account without signing in again. JDBC transactions coordinate related booking and payment updates; row locking protects concurrent reservations.

Persistent H2 SQL is the tested local default for customers, packages, vehicles, bookings, payments, reviews, messages, and guide assignments. Guide records are stored in `resources.txt` with atomic file replacement. Text reports are snapshots of SQL data, while the audit file records request metadata. PostgreSQL configuration is available but is not part of the verified local setup.

## Object-oriented design

The model uses private validated state, abstract base classes, inheritance, method overriding, interfaces, and dependency injection. Safari packages have distinct subtypes and pricing behavior; vehicles include jeep subtypes; payment processing uses a shared contract with cash and campus demonstration implementations. See the [class diagram](CLASS_DIAGRAM.md) and [viva guide](VIVA_GUIDE.md) for examples.

## Booking and payment rules

The API validates dates, departure slots, active catalogue entries, guest capacity, server-calculated prices, and vehicle schedule conflicts. Customers may change or cancel their own eligible reservations at least 24 hours before departure. A cash payment remains pending until staff records collection. The card option is a campus simulation: it accepts no card details, charges no money, and remains pending. Refund status represents a manually recorded return of collected cash. Bookings store a separate pickup place and their original safari/vehicle cost breakdown. History and receipts show the vehicle registration, payment amount, method, status and reference; the payment date is the recorded collection timestamp. Pending payments display Not paid yet.

## Fleet ownership and feedback

Administrators create, read, update and delete vehicle registration and owner details through the fleet form. Owner name, phone, address and ID/NIC stay in private administrator responses; public vehicle pages omit them. Omitted metadata is preserved on updates, while an explicit blank clears it. Deleting a vehicle with booking history remains restricted.

Customer and administrator reviews publish immediately, with no approval or text-edit action. Authors can delete their own reviews; administrators can delete any review and post replies. Account identity comes from the session, and public reviews omit email addresses. Fictional samples are labeled SQL records; a migration marker prevents deleted samples from returning. See [current account, booking and review behavior](ACCOUNT_BOOKING_REVIEWS.md).

## Quality and scope

The repository includes Java tests, static frontend checks, and browser QA scripts. See [QA instructions](QA_REPORT.md) for their scope and data requirements. Sample catalogue entries and labeled fictional reviews support demonstrations. Real card charging, outbound email delivery, production hosting, and live park availability are not implemented.

The six module responsibilities and related files are listed in [workload distribution](WORKLOAD_DISTRIBUTION.md) and the `team/` guides.
