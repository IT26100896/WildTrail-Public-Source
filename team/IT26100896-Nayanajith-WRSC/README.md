# Member 03 - Nayanajith W.R.S.C

Group: **26-MTR-Y1S2-WD-009**  
Student ID: **IT26100896**  
Student email: **IT26100896@my.sliit.lk**  
Component: **Booking and Reservation Management**

## Scope

Availability, date/time, guest count, booking status, customer change/cancellation and dashboard booking counts belong here. Coordinate payment state with Member 05 and guide availability with Member 04.

## Java files to understand and develop

- [`backend/src/main/java/com/wildtrail/modules/bookings/Booking.java`](../../backend/src/main/java/com/wildtrail/modules/bookings/Booking.java)
- [`backend/src/main/java/com/wildtrail/modules/bookings/BookingController.java`](../../backend/src/main/java/com/wildtrail/modules/bookings/BookingController.java)
- [`backend/src/main/java/com/wildtrail/modules/bookings/BookingService.java`](../../backend/src/main/java/com/wildtrail/modules/bookings/BookingService.java)
- [`backend/src/main/java/com/wildtrail/modules/bookings/JdbcBookingRepository.java`](../../backend/src/main/java/com/wildtrail/modules/bookings/JdbcBookingRepository.java)

### Read the Java flow in this order

1. **BookingController.java** receives a web request and passes it to the service. Read `create`, `mine`, `editMine`, and `cancelMine` first.
2. **BookingService.java** checks the customer, date, package, vehicle, price, payment state, and booking rules. Follow one method at a time; `create` saves a new booking, while `editMine` checks an existing booking before changing it.
3. **Booking.java** contains the small domain rules: valid status changes, price quote, departure slots, future dates, and 24-hour notice.
4. **JdbcBookingRepository.java** reads and writes booking rows. The SQL is split into readable blocks; `?` marks values supplied separately to the query.

For a new booking, follow **Controller → Service → Booking rules → Repository**. For a change or cancellation, the service first locks and checks the existing booking, then updates it through the repository. `@Transactional` keeps related database changes together if a step fails.

## UI files

- [`src/app/client/booking/page.tsx`](../../src/app/client/booking/page.tsx)
- [`src/app/client/my-bookings/page.tsx`](../../src/app/client/my-bookings/page.tsx)
- [`src/app/admin/(panel)/bookings/page.tsx`](../../src/app/admin/%28panel%29/bookings/page.tsx)
- [`src/app/admin/(panel)/dashboard/page.tsx`](../../src/app/admin/%28panel%29/dashboard/page.tsx)
- [`src/components/client/BookingWizard.tsx`](../../src/components/client/BookingWizard.tsx)
- [`src/components/client/QuickBooking.tsx`](../../src/components/client/QuickBooking.tsx)

## Shared files - change only your own function

- [`src/components/client/forms.tsx`](../../src/components/client/forms.tsx): MyBookingsList
- [`src/components/admin/managers.tsx`](../../src/components/admin/managers.tsx): BookingsManager
- [`src/components/client/sections.tsx`](../../src/components/client/sections.tsx): HowItWorks booking links
- [`src/types/domain.ts`](../../src/types/domain.ts): BookingDetail contracts

## Viva demonstration

Create -> history -> change guests/date -> cancel eligible booking; explain 24-hour rule, slot locking and state validation.

Pickup place is stored separately from special requests. The booking response includes vehicle registration, saved safari/vehicle costs, payment amount and collection date. Administrators can also create personal bookings, with the same ownership and notice checks as customers. See [booking and account details](../../docs/ACCOUNT_BOOKING_REVIEWS.md).

## Persistence and file handling

H2 SQL stores bookings and most other entities. Guide records use `resources.txt` file CRUD. The `/admin/files` page saves and reads text snapshots of SQL-backed modules.

## GitHub Desktop

1. Accept the private repository invitation sent to your student email and sign in to GitHub Desktop using your own GitHub account.
2. Clone the team repository in GitHub Desktop. Use Branch > New branch and name it `member/it26100896-bookings`.
3. Open the repository in your editor, inspect the paths above and make a real change in your module. Shared files may contain other members' code; edit only the relevant functions.
4. In GitHub Desktop inspect changed files and diff. Enter an accurate commit summary, click Commit to your branch, then Push origin.
5. Open a pull request into `main`. Wait for review and checks before merging. Do not upload `.local`, `backend/data`, `.env`, `node_modules`, `.next` or `backend/target`.

For IntelliJ IDEA open [`backend/pom.xml`](../../backend/pom.xml) as the Maven project. For full setup see [`README.md`](../../README.md) and [`docs/GITHUB_WORKFLOW.md`](../../docs/GITHUB_WORKFLOW.md).
