<div align="center">

# WildTrail

### Safari Booking System

Plan a Sri Lankan wildlife safari, manage reservations, and run the supporting staff workflows from one application.

![Course](https://img.shields.io/badge/SE1020-Object%20Oriented%20Programming-163b2b?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-d8a929?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-163b2b?style=for-the-badge)
![Next.js](https://img.shields.io/badge/Next.js-16.4.0-101820?style=for-the-badge)
![Group](https://img.shields.io/badge/Group-26--MTR--Y1S2--WD--009-d8a929?style=for-the-badge)

**[Get started](#get-started)** · **[Features](#features)** · **[Team](#team-and-responsibilities)** · **[Quality checks](#quality-checks)** · **[Documentation](#documentation)**

</div>

> [!NOTE]
> This is a university project and campus demonstration. Catalogue entries and labeled example reviews are sample content. Card payment is simulated: no card details are collected and no money is charged.

## At a glance

| Layer | Technology | Role |
|---|---|---|
| Website | Next.js 16, React 19, TypeScript | Customer pages, booking flow, and staff console |
| API | Java 17, Spring Boot 3.5 | Business rules, authentication, and CRUD endpoints |
| Data | Persistent H2 SQL; file storage for guides | Local records and `resources.txt` guide management |
| Verification | JUnit, ESLint, TypeScript, Playwright | Backend, build, and browser checks |

The six Java business modules live under `backend/src/main/java/com/wildtrail/modules/`. The website calls the Java API through the Next.js `/api` proxy. See [the class diagram](docs/CLASS_DIAGRAM.md) and [backend configuration](backend/README.md) for more detail.

## Get started

**Requirements:** Windows PowerShell, JDK 17, Node.js 22+, and an internet connection for the first Maven/npm dependency download. Open two PowerShell terminals in the project root. Set `JAVA_HOME` to your installed JDK 17 path if it differs from the example.

**Terminal 1 — configure and start the API**

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
powershell -ExecutionPolicy Bypass -File scripts\setup.ps1 -AdminEmail 'your-email@example.com'
powershell -ExecutionPolicy Bypass -File scripts\start-backend.ps1 -Build
```

**Terminal 2 — start the website**

```powershell
powershell -ExecutionPolicy Bypass -File scripts\start-frontend.ps1 -Production
```

Open **[the local website](http://localhost:3000/client/home)**. Customers and staff use the same **[Sign in](http://localhost:3000/client/login)** page; a staff account is routed to the admin console. The API listens on port `8080` and the website on port `3000`.

On first setup, the script generates a local administrator password in **`.local/admin-access.txt`**. Keep that file private. Existing `.local/config.json` and accounts are preserved on later runs; re-running setup does not reset a password. Every teammate should initialize their own local checkout with their own administrator email.

## Features

| Area | Implemented flow |
|---|---|
| Accounts | Customer registration, sign in/out, profile and password updates, protected staff routes, and administrator accounts |
| Safaris | Searchable destinations and packages, package management, and vehicle catalogue |
| Reservations | Multi-step booking, server-calculated pricing, availability checks, booking history, and eligible changes/cancellations with 24-hour notice |
| Resources | Guide CRUD in `resources.txt`, vehicle management, schedules, and guide assignment |
| Payments | Cash-on-arrival tracking, a **campus-only card demonstration**, staff-recorded collection/refunds, and printable receipts |
| Feedback | Customer reviews with own edit/delete and staff moderation; clearly labeled fictional examples; contact form and staff inbox |
| Reports | Save/read text snapshots for the six business areas |

The card demonstration keeps the payment **pending** and accepts no card number, expiry, CVV, or holder details. Real online payment, outbound email, verified park availability, and a production business identity are outside this project.

## Team and responsibilities

Group **26-MTR-Y1S2-WD-009** follows the six-component Safari Booking System workload distribution.

| Member | Student | Name | Module |
|:---:|---|---|---|
| 01 | IT26100894 | Mekhala B.G.R.S | [Users and customers](team/IT26100894-Mekhala-BGRS/README.md) |
| 02 | IT26100895 | Mihiran S.P.D | [Safaris and packages](team/IT26100895-Mihiran-SPD/README.md) |
| 03 | IT26100896 | Nayanajith W.R.S.C | [Bookings and reservations](team/IT26100896-Nayanajith-WRSC/README.md) |
| 04 | IT26100899 | Senevirathna M.K.K.T | [Guides and vehicles](team/IT26100899-Senevirathna-MKKT/README.md) |
| 05 | IT26100900 | Lakshani W.G.C | [Payments and receipts](team/IT26100900-Lakshani-WGC/README.md) |
| 06 | IT26100901 | Gamage C.I.P | [Reviews and feedback](team/IT26100901-Gamage-CIP/README.md) |

Select a module in the table, then click a file path in its guide to open the exact Java or UI source file. Each guide also covers shared code and a viva demonstration. The integrated application stays in **one repository**. Individual ZIPs in the sibling `Team-Member-Handoff` folder provide module reference copies. See the [workload distribution](docs/WORKLOAD_DISTRIBUTION.md) and [GitHub Desktop workflow](docs/GITHUB_WORKFLOW.md).

## Project layout

```text
backend/   Java API, Maven wrapper, and JUnit tests
database/  SQL schema and sample catalogue data
src/       Next.js pages, components, and typed API contracts
scripts/   Local setup, startup, and optional sharing commands
qa/        Repeatable browser QA scripts
docs/      Architecture, workload, QA, viva, and report material
team/      Member-specific module guides
```

The persistent local H2 database is stored at `backend/data/wildtrail.mv.db`. Do not run two API processes against the same H2 file. SQL is authoritative for most records; guide CRUD uses a real text file. Text reports are snapshots, not replacement databases. PostgreSQL configuration is available, but this handoff was verified with H2.

## Quality checks

Run these from the project root after dependencies are available:

```powershell
backend\mvnw.cmd -f backend\pom.xml test
npm.cmd ci
npm.cmd run typecheck
npm.cmd run lint
npm.cmd run build
```

Browser QA creates sample users and bookings. Run it only with a **disposable local database** and follow the guard instructions in the [QA report](docs/QA_REPORT.md):

```powershell
npx.cmd playwright install chromium
$env:QA_ALLOW_MUTATIONS = '1'
npm.cmd run qa:browser
```

Each member should understand their module, test their changes, and commit from their own GitHub account. The [viva guide](docs/VIVA_GUIDE.md) explains the implementation and its limits.

## Documentation

| Guide | Purpose |
|---|---|
| [Workload distribution](docs/WORKLOAD_DISTRIBUTION.md) | Six modules and cross-module ownership |
| [GitHub workflow](docs/GITHUB_WORKFLOW.md) | Clone, branch, commit, push, and pull-request steps |
| [Viva guide](docs/VIVA_GUIDE.md) | Demonstration flow and OOP explanations |
| [QA report](docs/QA_REPORT.md) | Test scope, evidence, and limits |
| [Class diagram](docs/CLASS_DIAGRAM.md) | Domain relationships |
| [Technical overview](docs/TECHNICAL_OVERVIEW.md) | Architecture, persistence, business rules, and scope |
| [Local public sharing](docs/PUBLIC_SHARING.md) | Optional temporary Cloudflare Quick Tunnel instructions |

> [!IMPORTANT]
> Keep `.local/`, environment files, databases, build output, logs, and credentials out of Git. A private repository limits visibility but does not replace careful handling of local secrets and customer data.
