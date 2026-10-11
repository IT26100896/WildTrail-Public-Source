# Member 04 - Senevirathna M.K.K.T

Group: **26-MTR-Y1S2-WD-009**  
Student ID: **IT26100899**  
Student email: **IT26100899@my.sliit.lk**  
Component: **Guide and Vehicle Management**

## Scope

Guide schedules, vehicle availability/maintenance fields, driver assignment and resources.txt file CRUD belong here.

## Java files to understand and develop

- [`backend/src/main/java/com/wildtrail/modules/resources/FamilySafariJeep.java`](../../backend/src/main/java/com/wildtrail/modules/resources/FamilySafariJeep.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/FileGuideRepository.java`](../../backend/src/main/java/com/wildtrail/modules/resources/FileGuideRepository.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/Guide.java`](../../backend/src/main/java/com/wildtrail/modules/resources/Guide.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/GuideController.java`](../../backend/src/main/java/com/wildtrail/modules/resources/GuideController.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/HiluxSafariJeep.java`](../../backend/src/main/java/com/wildtrail/modules/resources/HiluxSafariJeep.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/JdbcVehicleRepository.java`](../../backend/src/main/java/com/wildtrail/modules/resources/JdbcVehicleRepository.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/LuxuryJeep.java`](../../backend/src/main/java/com/wildtrail/modules/resources/LuxuryJeep.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/NaturalistGuide.java`](../../backend/src/main/java/com/wildtrail/modules/resources/NaturalistGuide.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/SafariJeep.java`](../../backend/src/main/java/com/wildtrail/modules/resources/SafariJeep.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/TeamController.java`](../../backend/src/main/java/com/wildtrail/modules/resources/TeamController.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/Vehicle.java`](../../backend/src/main/java/com/wildtrail/modules/resources/Vehicle.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/VehicleController.java`](../../backend/src/main/java/com/wildtrail/modules/resources/VehicleController.java)
- [`backend/src/main/java/com/wildtrail/modules/resources/VehicleService.java`](../../backend/src/main/java/com/wildtrail/modules/resources/VehicleService.java)

## UI files

- [`src/app/client/vehicles/page.tsx`](../../src/app/client/vehicles/page.tsx)
- [`src/app/admin/(panel)/vehicles/page.tsx`](../../src/app/admin/%28panel%29/vehicles/page.tsx)
- [`src/app/admin/(panel)/guides/page.tsx`](../../src/app/admin/%28panel%29/guides/page.tsx)

## Shared files - change only your own function

- [`src/components/client/cards.tsx`](../../src/components/client/cards.tsx): VehicleCard
- [`src/components/admin/managers.tsx`](../../src/components/admin/managers.tsx): VehiclesManager and GuidesManager
- [`src/app/client/about/page.tsx`](../../src/app/client/about/page.tsx): guide/team display

## Viva demonstration

Add guide/vehicle -> read -> update -> assign -> remove unused entry; restart to prove guide file persistence.

## Persistence and file handling

This app uses H2 SQL for most entities. Guides use real `resources.txt` file CRUD. Other modules can demonstrate Save/Read text snapshots at `/admin/files`; those snapshots are not the primary database. Do not claim that every module stores records only in `.txt` files.

## GitHub Desktop

1. Accept the private repository invitation sent to your student email and sign in to GitHub Desktop using your own GitHub account.
2. Clone the team repository in GitHub Desktop. Use Branch > New branch and name it `member/it26100899-resources`.
3. Open the repository in your editor, inspect the paths above and make a real change in your module. Shared files may contain other members' code; edit only the relevant functions.
4. In GitHub Desktop inspect changed files and diff. Enter an accurate commit summary, click Commit to your branch, then Push origin.
5. Open a pull request into `main`. Wait for review and checks before merging. Do not upload `.local`, `backend/data`, `.env`, `node_modules`, `.next` or `backend/target`.

For IntelliJ IDEA open [`backend/pom.xml`](../../backend/pom.xml) as the Maven project. For full setup see [`README.md`](../../README.md) and [`docs/GITHUB_WORKFLOW.md`](../../docs/GITHUB_WORKFLOW.md).
