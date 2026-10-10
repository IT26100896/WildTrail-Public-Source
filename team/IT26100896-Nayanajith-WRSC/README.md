# Member 03 - Nayanajith W.R.S.C

Group: **26-MTR-Y1S2-WD-009**  
Student ID: **IT26100896**  
Student email: **IT26100896@my.sliit.lk**  
Component: **Booking and Reservation Management**

## Scope

Availability, date/time, guest count, booking status, customer change/cancellation and dashboard booking counts belong here. Coordinate payment state with Member 05 and guide availability with Member 04.

## Java files to understand and develop

- `backend/src/main/java/com/wildtrail/modules/bookings/Booking.java`
- `backend/src/main/java/com/wildtrail/modules/bookings/BookingController.java`
- `backend/src/main/java/com/wildtrail/modules/bookings/BookingService.java`
- `backend/src/main/java/com/wildtrail/modules/bookings/JdbcBookingRepository.java`

## UI files

- `src/app/client/booking/page.tsx`
- `src/app/client/my-bookings/page.tsx`
- `src/app/admin/(panel)/bookings/page.tsx`
- `src/app/admin/(panel)/dashboard/page.tsx`
- `src/components/client/BookingWizard.tsx`
- `src/components/client/QuickBooking.tsx`

## Shared files - change only your own function

- `src/components/client/forms.tsx: MyBookingsList`
- `src/components/admin/managers.tsx: BookingsManager`
- `src/components/client/sections.tsx: HowItWorks booking links`
- `src/types/domain.ts: BookingDetail contracts`

## Viva demonstration

Create -> history -> change guests/date -> cancel eligible booking; explain 24-hour rule, slot locking and state validation.

## Persistence and file handling

This app uses H2 SQL for most entities. Guides use real `resources.txt` file CRUD. Other modules can demonstrate Save/Read text snapshots at `/admin/files`; those snapshots are not the primary database. Do not claim that every module stores records only in `.txt` files.

## GitHub Desktop

1. Accept the private repository invitation sent to your student email and sign in to GitHub Desktop using your own GitHub account.
2. Clone the team repository in GitHub Desktop. Use Branch > New branch and name it `member/it26100896-bookings`.
3. Open the repository in your editor, inspect the paths above and make a real change in your module. Shared files may contain other members' code; edit only the relevant functions.
4. In GitHub Desktop inspect changed files and diff. Enter an accurate commit summary, click Commit to your branch, then Push origin.
5. Open a pull request into `main`. Wait for review and checks before merging. Do not upload `.local`, `backend/data`, `.env`, `node_modules`, `.next` or `backend/target`.

For IntelliJ IDEA open `backend/pom.xml` as the Maven project. For full setup see `README.md` and `docs/GITHUB_WORKFLOW.md`.
