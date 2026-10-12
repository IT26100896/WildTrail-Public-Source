# Member 02 - Mihiran S.P.D

Group: **26-MTR-Y1S2-WD-009**  
Student ID: **IT26100895**  
Student email: **IT26100895@my.sliit.lk**  
Component: **Safari Tour and Package Management**

## Scope

Destinations, public catalogue cards, sample catalogue seed and search/filter belong with safari packages.

## Java files to understand and develop

- [`backend/src/main/java/com/wildtrail/modules/safaris/EveningSafari.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/EveningSafari.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/FamilySafari.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/FamilySafari.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/FullDaySafari.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/FullDaySafari.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/HalfDaySafari.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/HalfDaySafari.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/JdbcPackageRepository.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/JdbcPackageRepository.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/MorningSafari.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/MorningSafari.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/PackageController.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/PackageController.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/PackageService.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/PackageService.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/PhotographySafari.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/PhotographySafari.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/PrivateSafari.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/PrivateSafari.java)
- [`backend/src/main/java/com/wildtrail/modules/safaris/SafariPackage.java`](../../backend/src/main/java/com/wildtrail/modules/safaris/SafariPackage.java)
- [`backend/src/main/java/com/wildtrail/config/DatabaseSeeder.java`](../../backend/src/main/java/com/wildtrail/config/DatabaseSeeder.java)

## UI files

- [`src/app/client/safaris/page.tsx`](../../src/app/client/safaris/page.tsx)
- [`src/app/client/packages/page.tsx`](../../src/app/client/packages/page.tsx)
- [`src/app/client/destinations/page.tsx`](../../src/app/client/destinations/page.tsx)
- [`src/app/admin/(panel)/packages/page.tsx`](../../src/app/admin/%28panel%29/packages/page.tsx)
- [`src/components/client/cards.tsx`](../../src/components/client/cards.tsx)

## Shared files - change only your own function

- [`src/components/client/forms.tsx`](../../src/components/client/forms.tsx): PackageExplorer
- [`src/components/admin/managers.tsx`](../../src/components/admin/managers.tsx): PackagesManager
- [`src/components/client/BookingWizard.tsx`](../../src/components/client/BookingWizard.tsx): package selection
- [`src/lib/content.ts`](../../src/lib/content.ts): catalogue display data

## Viva demonstration

Create package -> read/filter catalogue -> change price -> delete unused package; explain SafariPackage subclasses and pricing.

## Persistence and file handling

H2 SQL stores safari packages and most other entities. Guide records use `resources.txt` file CRUD. The `/admin/files` page saves and reads text snapshots of SQL-backed modules.

## GitHub Desktop

1. Accept the private repository invitation sent to your student email and sign in to GitHub Desktop using your own GitHub account.
2. Clone the team repository in GitHub Desktop. Use Branch > New branch and name it `member/it26100895-safaris`.
3. Open the repository in your editor, inspect the paths above and make a real change in your module. Shared files may contain other members' code; edit only the relevant functions.
4. In GitHub Desktop inspect changed files and diff. Enter an accurate commit summary, click Commit to your branch, then Push origin.
5. Open a pull request into `main`. Wait for review and checks before merging. Do not upload `.local`, `backend/data`, `.env`, `node_modules`, `.next` or `backend/target`.

For IntelliJ IDEA open [`backend/pom.xml`](../../backend/pom.xml) as the Maven project. For full setup see [`README.md`](../../README.md) and [`docs/GITHUB_WORKFLOW.md`](../../docs/GITHUB_WORKFLOW.md).
