# Quality assurance

## Automated checks

| Check | Command | Coverage |
|---|---|---|
| Java tests | `backend\mvnw.cmd -f backend\pom.xml test` | API, domain, repository, security, and booking/payment rules |
| TypeScript | `npm.cmd run typecheck` | Frontend type contracts |
| ESLint | `npm.cmd run lint` | Frontend source quality |
| Production build | `npm.cmd run build` | Next.js compilation and route generation |
| Browser flows | `npm.cmd run qa:browser` | Public routes, authentication, booking, payment, profile, review, and staff pages |

Install dependencies with `npm.cmd ci` and use JDK 17 for the backend. Run the first four checks from the repository root. Test result files are generated locally and are not committed.

## Browser test data

The browser script in `qa/browser.mjs` creates customer, booking, payment, and review records. Start the website and API with a **disposable local database**, install Chromium with `npx.cmd playwright install chromium`, then run:

```powershell
$env:QA_ALLOW_MUTATIONS = '1'
npm.cmd run qa:browser
```

The script refuses to run without `QA_ALLOW_MUTATIONS=1`. Screenshots are written to `qa/screenshots/`, which is excluded from Git. Never run this suite against customer data.

## Manual review areas

- Verify navigation, booking controls, and admin tables at normal browser zoom and narrower viewport widths.
- Verify sign out and protected-route redirects for customer and staff sessions.
- Verify the card demonstration remains pending and requests no card information.
- Verify fictional example reviews are visibly labeled and excluded from customer rating totals.
- Verify guide file persistence after restarting the API.

These checks cover a local demonstration environment. They do not establish production hosting, real gateway operation, outbound email delivery, or live safari availability.
