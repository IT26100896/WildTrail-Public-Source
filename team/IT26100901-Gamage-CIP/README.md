# Member 06 - Gamage C.I.P

Group: **26-MTR-Y1S2-WD-009**  
Student ID: **IT26100901**  
Student email: **IT26100901@my.sliit.lk**  
Component: **Review and Rating Management**

## Scope

Review publication, administrator replies, deletion, clearly labeled fictional sample reviews, customer feedback and contact inbox belong here.

## Java files to understand and develop

- [`backend/src/main/java/com/wildtrail/modules/reviews/JdbcReviewRepository.java`](../../backend/src/main/java/com/wildtrail/modules/reviews/JdbcReviewRepository.java)
- [`backend/src/main/java/com/wildtrail/modules/reviews/MyReviewsController.java`](../../backend/src/main/java/com/wildtrail/modules/reviews/MyReviewsController.java)
- [`backend/src/main/java/com/wildtrail/modules/reviews/Review.java`](../../backend/src/main/java/com/wildtrail/modules/reviews/Review.java)
- [`backend/src/main/java/com/wildtrail/modules/reviews/ReviewController.java`](../../backend/src/main/java/com/wildtrail/modules/reviews/ReviewController.java)
- [`backend/src/main/java/com/wildtrail/modules/reviews/ReviewService.java`](../../backend/src/main/java/com/wildtrail/modules/reviews/ReviewService.java)
- [`backend/src/main/java/com/wildtrail/modules/reviews/VerifiedReview.java`](../../backend/src/main/java/com/wildtrail/modules/reviews/VerifiedReview.java)
- [`backend/src/main/java/com/wildtrail/controller/ContactController.java`](../../backend/src/main/java/com/wildtrail/controller/ContactController.java)
- [`backend/src/main/java/com/wildtrail/controller/InboxController.java`](../../backend/src/main/java/com/wildtrail/controller/InboxController.java)

## UI files

- [`src/app/client/reviews/page.tsx`](../../src/app/client/reviews/page.tsx)
- [`src/app/client/my-reviews/page.tsx`](../../src/app/client/my-reviews/page.tsx)
- [`src/app/client/contact/page.tsx`](../../src/app/client/contact/page.tsx)
- [`src/app/admin/(panel)/reviews/page.tsx`](../../src/app/admin/%28panel%29/reviews/page.tsx)
- [`src/app/admin/(panel)/messages/page.tsx`](../../src/app/admin/%28panel%29/messages/page.tsx)
- [`src/components/client/MyReviews.tsx`](../../src/components/client/MyReviews.tsx)
- [`src/components/client/ReviewReplies.tsx`](../../src/components/client/ReviewReplies.tsx)
- [Sample review migration](../../database/schema.sql)

## Shared files - change only your own function

- [`src/components/client/forms.tsx`](../../src/components/client/forms.tsx): ReviewForm and ContactForm
- [`src/components/admin/managers.tsx`](../../src/components/admin/managers.tsx): ReviewsManager
- [`src/components/client/interactive.tsx`](../../src/components/client/interactive.tsx): ReviewCarousel
- [`src/components/client/sections.tsx`](../../src/components/client/sections.tsx): loadPublicReviews

## Viva demonstration

Submit -> immediate public display -> administrator reply -> owner/admin delete; explain real reviews versus fictional examples and verified review subtype.

## Persistence and file handling

H2 SQL stores reviews and most other entities. Guide records use `resources.txt` file CRUD. The `/admin/files` page saves and reads text snapshots of SQL-backed modules.

## GitHub Desktop

1. Accept the private repository invitation sent to your student email and sign in to GitHub Desktop using your own GitHub account.
2. Clone the team repository in GitHub Desktop. Use Branch > New branch and name it `member/it26100901-reviews`.
3. Open the repository in your editor, inspect the paths above and make a real change in your module. Shared files may contain other members' code; edit only the relevant functions.
4. In GitHub Desktop inspect changed files and diff. Enter an accurate commit summary, click Commit to your branch, then Push origin.
5. Open a pull request into `main`. Wait for review and checks before merging. Do not upload `.local`, `backend/data`, `.env`, `node_modules`, `.next` or `backend/target`.

For IntelliJ IDEA open [`backend/pom.xml`](../../backend/pom.xml) as the Maven project. For full setup see [`README.md`](../../README.md) and [`docs/GITHUB_WORKFLOW.md`](../../docs/GITHUB_WORKFLOW.md).
