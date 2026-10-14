# Quality assurance

## Automated checks

| Check | Command | Coverage |
|---|---|---|
| Java tests | `backend\mvnw.cmd -f backend\pom.xml test` | API, domain, repository, security, and booking/payment rules |
| TypeScript | `npm.cmd run typecheck` | Frontend type contracts |
| ESLint | `npm.cmd run lint` | Frontend source quality |
| Production build | `npm.cmd run build` | Next.js compilation and route generation |
| Browser flows | `npm.cmd run qa:browser` | Public routes, authentication, booking, payment, profile, review, and staff pages |
| Account and review changes | `node qa/account-booking-reviews.mjs` | Administrator website access and personal bookings, payment details/dates, verified identity, public replies, sample deletion and responsive layouts |
| Vehicle ownership | `node qa/vehicle-owner.mjs` | Vehicle registration and owner create/read/update/delete, mobile editing, saved-field reload and public API privacy |
| Navigation and access | `node qa/auth-navigation.mjs` | Responsive navigation, staff logout, administrator creation, and customer access limits |
| Responsive layout | `node qa/responsive-fixes.mjs` | Horizontal overflow and element placement at 320–2133 px |

Install dependencies with `npm.cmd ci` and use JDK 17 for the backend. Run the first four checks from the repository root. Test result files are generated locally and are not committed.

### Account, booking and review update — 14 October 2026

Java 17 Maven verification passed **66 tests**. TypeScript, ESLint and the Next.js production build passed. Browser verification passed **11 general flows**, **9 administrator/booking/review flows** and **4 navigation/access checks** using an isolated H2 database. The new flow checks include administrator personal bookings, pickup/payment details, recorded payment date, immediate review publication, staff replies, deletion, correct name/email attribution and layouts from 320 to 1920 px. The navigation suite also covered effective viewport widths for zoom reflow.

SQL initialization was tested again after removing reviews: the migration marker prevented deleted samples from being reinserted. Vehicle registration validation, duplicate plates, optional-field compatibility and administrator profile/password changes were checked by the Java suite.

The records below describe earlier QA snapshots and their original versions.

### Vehicle owner details — 14 October 2026

The Java test suite passed **69 tests**, including per-vehicle owner storage, public/customer response privacy, owner field validation, omitted-field preservation and explicit clearing. TypeScript, ESLint, backend packaging and the Next.js production build passed. The vehicle owner browser suite passed **4 flows**: create, read/update/reload on mobile, public privacy and delete. Registration numbers were also created, updated, read back after reload and removed with the vehicle.

## Browser test data

The browser script in `qa/browser.mjs` creates customer, booking, payment, and review records. Start the website and API with a **disposable local database**, install Chromium with `npx.cmd playwright install chromium`, then run:

```powershell
$env:QA_ALLOW_MUTATIONS = '1'
npm.cmd run qa:browser
```

The script refuses to run without `QA_ALLOW_MUTATIONS=1`. Screenshots are written to `qa/screenshots/`, which is excluded from Git. Never run this suite against customer data.

## Local verification record — 11 October 2026

The integrated Java 17 Maven suite passed **57 tests**. `npm run lint`, `npm run typecheck`, and `npm run build` passed. With a disposable in-memory H2 database, the browser flow suite passed **11 checks**, navigation/access passed **4 checks**, and responsive layout passed across **12 widths** from 320 to 2133 px. A separate visual inspection at 390 and 1440 px found no broken images or unrevealed elements. CSS viewport tests approximate screen-size and zoom reflow; they do not operate the browser's native zoom controls.

`npm audit --omit=dev --audit-level=moderate` reported **0 production dependency findings**. A full `npm audit` reported **5 high-severity entries** in one development-only ESLint dependency chain (`eslint-config-next` → `@next/eslint-plugin-next` → `fast-glob` → `micromatch` → `braces`). The installed `braces` version is 3.0.3; the advisory lists no patched version as of this check. Do not feed untrusted glob patterns to the lint tooling. Recheck the advisory when a maintained upstream fix is published.

The checks were run locally against H2. GitHub Actions, native zoom in every browser, PostgreSQL deployment, and hosted operation require separate verification.

## Manual review areas

- Verify navigation, booking controls, and admin tables at normal browser zoom and narrower viewport widths.
- Verify sign out and protected-route redirects for customer and staff sessions.
- Verify the card demonstration remains pending and requests no card information.
- Verify fictional example reviews are visibly labeled and excluded from customer rating totals.
- Verify guide file persistence after restarting the API.

These checks cover a local demonstration environment. They do not establish production hosting, real gateway operation, outbound email delivery, or live safari availability.
