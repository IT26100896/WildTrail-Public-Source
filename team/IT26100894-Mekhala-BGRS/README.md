# Member 01 - Mekhala B.G.R.S

Group: **26-MTR-Y1S2-WD-009**  
Student ID: **IT26100894**  
Student email: **IT26100894@my.sliit.lk**  
Component: **User and Customer Management**

## Scope

Administrator creation, protected accounts, login/logout, password changes and customer profile are part of this module.

## Java files to understand and develop

- `backend/src/main/java/com/wildtrail/modules/users/Admin.java`
- `backend/src/main/java/com/wildtrail/modules/users/AdminAccountController.java`
- `backend/src/main/java/com/wildtrail/modules/users/AuthController.java`
- `backend/src/main/java/com/wildtrail/modules/users/AuthService.java`
- `backend/src/main/java/com/wildtrail/modules/users/Customer.java`
- `backend/src/main/java/com/wildtrail/modules/users/CustomerController.java`
- `backend/src/main/java/com/wildtrail/modules/users/CustomerService.java`
- `backend/src/main/java/com/wildtrail/modules/users/JdbcCustomerRepository.java`
- `backend/src/main/java/com/wildtrail/modules/users/JdbcUserRepository.java`
- `backend/src/main/java/com/wildtrail/modules/users/ProfileController.java`
- `backend/src/main/java/com/wildtrail/modules/users/User.java`
- `backend/src/main/java/com/wildtrail/config/AdminBootstrap.java`
- `backend/src/main/java/com/wildtrail/controller/SessionController.java`
- `backend/src/main/java/com/wildtrail/security/SessionCookieService.java`
- `backend/src/main/java/com/wildtrail/security/SessionAuthenticationFilter.java`
- `backend/src/main/java/com/wildtrail/security/LoginThrottle.java`

## UI files

- `src/app/client/login/page.tsx`
- `src/app/client/register/page.tsx`
- `src/app/client/profile/page.tsx`
- `src/app/admin/login/page.tsx`
- `src/app/admin/(panel)/accounts/page.tsx`
- `src/app/admin/(panel)/customers/page.tsx`
- `src/components/admin/AdminAccountsManager.tsx`
- `src/components/admin/AdminLoginForm.tsx`
- `src/components/client/AuthPanel.tsx`
- `src/components/client/ProfileForm.tsx`

## Shared files - change only your own function

- `src/components/client/forms.tsx: AuthForm`
- `src/components/admin/managers.tsx: CustomersManager`
- `src/components/client/Navbar.tsx: login/logout navigation`
- `src/lib/session.ts: session lookup`

## Viva demonstration

Register -> sign in -> update profile -> delete unused account; show administrator creation and role protection.

## Persistence and file handling

This app uses H2 SQL for most entities. Guides use real `resources.txt` file CRUD. Other modules can demonstrate Save/Read text snapshots at `/admin/files`; those snapshots are not the primary database. Do not claim that every module stores records only in `.txt` files.

## GitHub Desktop

1. Accept the private repository invitation sent to your student email and sign in to GitHub Desktop using your own GitHub account.
2. Clone the team repository in GitHub Desktop. Use Branch > New branch and name it `member/it26100894-users`.
3. Open the repository in your editor, inspect the paths above and make a real change in your module. Shared files may contain other members' code; edit only the relevant functions.
4. In GitHub Desktop inspect changed files and diff. Enter an accurate commit summary, click Commit to your branch, then Push origin.
5. Open a pull request into `main`. Wait for review and checks before merging. Do not upload `.local`, `backend/data`, `.env`, `node_modules`, `.next` or `backend/target`.

For IntelliJ IDEA open `backend/pom.xml` as the Maven project. For full setup see `README.md` and `docs/GITHUB_WORKFLOW.md`.
