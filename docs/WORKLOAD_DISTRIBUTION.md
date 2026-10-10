# Safari Booking System - team workload

**Group ID: 26-MTR-Y1S2-WD-009**  
**Course: SE1020 Object Oriented Programming**

The six components follow the supplied Safari Booking System workload guide. Java class counts do not measure effort: booking rules and transactions are complex even though that module has fewer classes. Each member owns a business flow, its Java code, relevant UI, documentation, testing and viva explanation.

| Member | Student and name | Main module | Additional features in this website |
|---|---|---|---|
| 01 | IT26100894 - Mekhala B.G.R.S | User and Customer Management | Administrator accounts, login/logout, passwords, session protection and customer profile. |
| 02 | IT26100895 - Mihiran S.P.D | Safari Tour and Package Management | Destinations, catalogue seed, package cards and public search/filter. |
| 03 | IT26100896 - Nayanajith W.R.S.C | Booking and Reservation Management | Availability, date/guest changes, cancellation, booking dashboard and quick booking. |
| 04 | IT26100899 - Senevirathna M.K.K.T | Guide and Vehicle Management | Guide schedules, vehicle availability, driver assignment and real `resources.txt` file CRUD. |
| 05 | IT26100900 - Lakshani W.G.C | Payment and Transaction Management | Cash collection, campus-only card demo, receipt view and text file reports. |
| 06 | IT26100901 - Gamage C.I.P | Review and Rating Management | Customer feedback, contact inbox, moderation and clearly labeled fictional examples. |

Each member's exact Java and UI paths, shared functions and viva demonstration are in their named `team/IT.../README.md` folder. Shared files such as `src/components/admin/managers.tsx`, `src/components/client/forms.tsx`, `src/components/client/BookingWizard.tsx`, `src/components/client/cards.tsx` and `src/components/client/sections.tsx` contain multiple responsibilities. Edit only the relevant function and ask the affected module owner to review changes.

Cross-module review: Member 01 and 03 check session/booking ownership; Member 03 and 04 check schedule conflicts; Member 03 and 05 check booking/payment state; Member 02 and 06 check catalogue/review presentation. Security configuration, database schema, responsive navigation and CI need team review.

The project uses H2 SQL for most records and actual file read/write for guides; SQL snapshot reports are secondary exports. This distinction is reflected in the module guides and viva examples.

The table assigns module ownership for development, testing, documentation, and viva preparation. Cross-module changes should be reviewed by the affected members.
