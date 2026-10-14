# Member 05 - Lakshani W.G.C

Group: **26-MTR-Y1S2-WD-009**  
Student ID: **IT26100900**  
Student email: **IT26100900@my.sliit.lk**  
Component: **Payment and Transaction Management**

## Scope

Cash collection, campus-only demo card state, receipts and text file reports belong here. Card details and real charging are not implemented.

## Java files to understand and develop

- [`backend/src/main/java/com/wildtrail/modules/payments/CardPayment.java`](../../backend/src/main/java/com/wildtrail/modules/payments/CardPayment.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/CashPayment.java`](../../backend/src/main/java/com/wildtrail/modules/payments/CashPayment.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/DemoCardPayment.java`](../../backend/src/main/java/com/wildtrail/modules/payments/DemoCardPayment.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/JdbcPaymentRepository.java`](../../backend/src/main/java/com/wildtrail/modules/payments/JdbcPaymentRepository.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/Payment.java`](../../backend/src/main/java/com/wildtrail/modules/payments/Payment.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/PaymentController.java`](../../backend/src/main/java/com/wildtrail/modules/payments/PaymentController.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/PaymentFactory.java`](../../backend/src/main/java/com/wildtrail/modules/payments/PaymentFactory.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/PaymentProcessor.java`](../../backend/src/main/java/com/wildtrail/modules/payments/PaymentProcessor.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/PaymentResult.java`](../../backend/src/main/java/com/wildtrail/modules/payments/PaymentResult.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/PaymentService.java`](../../backend/src/main/java/com/wildtrail/modules/payments/PaymentService.java)
- [`backend/src/main/java/com/wildtrail/modules/payments/ReceiptController.java`](../../backend/src/main/java/com/wildtrail/modules/payments/ReceiptController.java)
- [`backend/src/main/java/com/wildtrail/controller/FileReportController.java`](../../backend/src/main/java/com/wildtrail/controller/FileReportController.java)

## UI files

- [Personal payment history](../../src/app/client/payments/page.tsx)
- [Shared payment summary and date formatting](../../src/components/client/PaymentSummary.tsx)

- [`src/app/admin/(panel)/payments/page.tsx`](../../src/app/admin/%28panel%29/payments/page.tsx)
- [`src/app/admin/(panel)/files/page.tsx`](../../src/app/admin/%28panel%29/files/page.tsx)
- [`src/app/client/receipts/[id]/page.tsx`](../../src/app/client/receipts/%5Bid%5D/page.tsx)
- [`src/components/client/PrintButton.tsx`](../../src/components/client/PrintButton.tsx)

## Shared files - change only your own function

- [`src/components/client/BookingWizard.tsx`](../../src/components/client/BookingWizard.tsx): payment and confirmation steps
- [`src/components/admin/managers.tsx`](../../src/components/admin/managers.tsx): PaymentsManager and FileReportsManager
- [`src/app/admin/(panel)/dashboard/page.tsx`](../../src/app/admin/%28panel%29/dashboard/page.tsx): revenue display

## Viva demonstration

Create pending payment -> read receipt -> record actual cash -> refund or remove failed record; explain PaymentProcessor polymorphism.

## Persistence and file handling

H2 SQL stores payments and most other entities. Guide records use `resources.txt` file CRUD. The `/admin/files` page saves and reads text snapshots of SQL-backed modules.

## GitHub Desktop

1. Accept the private repository invitation sent to your student email and sign in to GitHub Desktop using your own GitHub account.
2. Clone the team repository in GitHub Desktop. Use Branch > New branch and name it `member/it26100900-payments`.
3. Open the repository in your editor, inspect the paths above and make a real change in your module. Shared files may contain other members' code; edit only the relevant functions.
4. In GitHub Desktop inspect changed files and diff. Enter an accurate commit summary, click Commit to your branch, then Push origin.
5. Open a pull request into `main`. Wait for review and checks before merging. Do not upload `.local`, `backend/data`, `.env`, `node_modules`, `.next` or `backend/target`.

For IntelliJ IDEA open [`backend/pom.xml`](../../backend/pom.xml) as the Maven project. For full setup see [`README.md`](../../README.md) and [`docs/GITHUB_WORKFLOW.md`](../../docs/GITHUB_WORKFLOW.md).
