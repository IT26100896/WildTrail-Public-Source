# Website accounts, booking details and review replies

An administrator can open the website through the console logo or **View website**, then use **Admin account → Admin dashboard** to return. The website menu shows the account name, email and role. The same account can make personal bookings, view its booking/payment history, update its profile, submit reviews and send contact messages. Personal history remains restricted to that account; staff management pages retain their administrator-only checks.

## Booking and payment details

- **Pickup place** is a separate booking field, collected in the details step and shown in confirmation, booking history, staff booking details and the printable statement.
- **Vehicle number** is the registration plate recorded under **Admin → Vehicles → Edit**. Existing vehicles without a plate show **Pending assignment**.
- **Vehicle owner details** include full name, telephone number, address and ID/NIC. Administrators can add/edit them in the fleet form and expand **Owner details (admin only)** on the vehicle card. Public vehicle APIs and pages exclude these details. Old records can remain empty; updates from older clients preserve omitted owner fields. An explicit blank clears an owner field. Deleting an unused vehicle removes its metadata; the existing booking-history deletion restriction still applies.
- **Payment summary** shows the saved safari cost, vehicle hire, total and recorded payment amount. Existing bookings without a saved cost breakdown show **Included in total**, without recalculating historical prices.
- **Payment details** include method, status and transaction reference. **My Payments** contains the signed-in account's payment history.
- **Payment date** comes from the actual collection timestamp. Pending payments show **Not paid yet**. A campus card demonstration remains pending and charges no money.

## Reviews and identity

Reviews publish immediately. Review text and ratings cannot be edited or hidden through an approval action. Authors can delete their own reviews; administrators can delete any review. Administrators can add replies on the public review page or in **Admin → Reviews**. Deleting a review also deletes its replies.

The review dashboard separates customer reviews, administrator reviews and fictional samples, with search by name, email or text. Names, emails and roles come from the authenticated account. Public pages show names and administrator badges; email addresses are reserved for the author and staff views.

Fictional samples are stored in SQL, visibly labeled, and can be deleted from the website. A one-time migration marker prevents deleted samples from returning on restart. Previous pending reviews are published by the migration.

## Relevant files

- [Booking API](../backend/src/main/java/com/wildtrail/modules/bookings/BookingController.java) and [booking service](../backend/src/main/java/com/wildtrail/modules/bookings/BookingService.java)
- [Booking wizard](../src/components/client/BookingWizard.tsx) and [payment summary](../src/components/client/PaymentSummary.tsx)
- [Personal payments page](../src/app/client/payments/page.tsx) and [receipt page](../src/app/client/receipts/%5Bid%5D/page.tsx)
- [Website sessions](../src/lib/session.ts), [navigation](../src/components/client/Navbar.tsx) and [profile API](../backend/src/main/java/com/wildtrail/modules/users/ProfileController.java)
- [Review API](../backend/src/main/java/com/wildtrail/modules/reviews/ReviewController.java), [review repository](../backend/src/main/java/com/wildtrail/modules/reviews/JdbcReviewRepository.java) and [reply component](../src/components/client/ReviewReplies.tsx)
- [Vehicle API](../backend/src/main/java/com/wildtrail/modules/resources/VehicleController.java), [vehicle service](../backend/src/main/java/com/wildtrail/modules/resources/VehicleService.java), [vehicle repository](../backend/src/main/java/com/wildtrail/modules/resources/JdbcVehicleRepository.java) and [fleet form](../src/components/admin/managers.tsx)
- [Schema and upgrade](../database/schema.sql)

## Verification

Use Java 17 for `backend\mvnw.cmd -f backend\pom.xml verify`, then run `npm run typecheck`, `npm run lint` and `npm run build`.

Browser checks require a disposable database and `QA_ALLOW_MUTATIONS=1`. Run `node qa/browser.mjs`, `node qa/account-booking-reviews.mjs` and `node qa/vehicle-owner.mjs`. Set `QA_CONFIG_PATH` to an ignored local JSON file containing the test administrator's credentials when the QA database uses separate accounts. These scripts create demonstration records and must not run against customer data.
