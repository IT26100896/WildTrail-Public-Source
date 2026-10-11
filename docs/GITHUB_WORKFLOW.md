# GitHub Desktop workflow for all six members

Group **26-MTR-Y1S2-WD-009** uses one private GitHub repository. The project owner publishes the integrated baseline once. Each invited student accepts the email invitation to their own GitHub account. Student mail domain is `@my.sliit.lk`.

## Repository and member tasks

Repository: [IT26100896/WildTrail-Public-Source](https://github.com/IT26100896/WildTrail-Public-Source).

| Student | Component issue | Working branch |
|---|---|---|
| IT26100894 | [Users and customers #1](https://github.com/IT26100896/WildTrail-Public-Source/issues/1) | `member/it26100894-users` |
| IT26100895 | [Safaris and packages #2](https://github.com/IT26100896/WildTrail-Public-Source/issues/2) | `member/it26100895-safaris` |
| IT26100896 | [Bookings and reservations #3](https://github.com/IT26100896/WildTrail-Public-Source/issues/3) | `member/it26100896-bookings` |
| IT26100899 | [Guides and vehicles #4](https://github.com/IT26100896/WildTrail-Public-Source/issues/4) | `member/it26100899-resources` |
| IT26100900 | [Payments and receipts #5](https://github.com/IT26100896/WildTrail-Public-Source/issues/5) | `member/it26100900-payments` |
| IT26100901 | [Reviews and feedback #6](https://github.com/IT26100896/WildTrail-Public-Source/issues/6) | `member/it26100901-reviews` |

The integrated baseline is already published. Clone this repository instead of publishing another copy or uploading a complete module ZIP over existing files.

## Owner: repository administration

1. In GitHub Desktop, sign in as `IT26100896`. Choose **File > Add local repository** and select the folder containing this `README.md` (the `.git` folder is already prepared locally).
2. Check the changed-files list. The source must not include `.local/`, `.env*`, `node_modules/`, `.next/`, `backend/data/`, `backend/target/` or real account data.
3. The private repository is `IT26100896/WildTrail-Public-Source`. Confirm that `origin` points to it and push `main`. If starting from a fresh copy without a remote, publish under the owner account with **Keep this code private** selected.
4. On GitHub open **Settings > Collaborators > Add people**. Invite the other five students via their exact `@my.sliit.lk` emails. They must accept. This requires owner/admin permission.
5. Keep `main` protection enabled: one approving review, the passing `verify` check with an up-to-date branch, resolved review conversations, and no force pushes or branch deletion. The repository owner can bypass some review/check requirements; use the same review workflow as the team.

## Every member: actual contribution

1. Accept your invite, log in to GitHub Desktop with your own student account, then **File > Clone repository**. Never use another member's login.
2. Open **File > Options > Git**, set your own name, and choose an email associated with your GitHub account. Use your verified student email or your account's GitHub-provided no-reply email. Signing in and setting the commit author are separate steps.
3. Select `main`, click **Fetch origin**, and **Pull origin** if offered. Choose **Branch > New branch** with your working branch from the table. If it already exists, choose it in **Current Branch** instead of creating it again.
4. Read your component issue and `team/IT.../README.md`. Open `backend/pom.xml` in IntelliJ IDEA. Review your Java module and its UI routes, make a concrete improvement or fix, and run the relevant checks. Coordinate edits to shared UI files with the other affected member.
5. Return to GitHub Desktop, inspect the changed files and diff, write an accurate commit summary, click **Commit to branch**, then **Publish branch** or **Push origin**. The baseline alone will not create a new change to commit.
6. Click **Preview Pull Request** or **Create Pull Request**. Use `main` as the base and your working branch as the compare branch. Describe the change, the related component issue, and the checks you actually ran.
7. Request a review from another teammate. The author cannot approve their own pull request. Fix review comments on the same branch and push again; a new code change dismisses the earlier approval, so request a fresh review.
8. Resolve conversations and wait for the green `verify` check. If GitHub requires an update, update the branch from `main`, resolve conflicts carefully, and rerun checks. The owner/team can then merge with **Create a merge commit** to preserve the individual commit history.
9. After a merge, select `main` in GitHub Desktop, **Fetch origin**, and **Pull origin**. Start the next task from the updated `main` using a new branch. Retain accurate issue, commit, and PR links for the contribution record and viva.

The `Team-Member-Handoff` folders outside the repository contain module reference copies for study and offline sharing. Make changes in the integrated repository. Shared UI files should be edited at the relevant function rather than replaced wholesale from a reference copy.

## Checks before a pull request

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
backend\mvnw.cmd -f backend\pom.xml test
npm ci
npm run typecheck
npm run lint
npm run build
```

Record which checks ran and their results in the pull request. Do not run browser QA against real customer data; it creates demo records.

## Review and viva evidence

Explain the trigger, changed classes/functions, validation, and result of your own contribution. A pull request review records another member's review work; it does not replace their own implementation work. GitHub history documents contributions, while the course marking guide and viva determine assessment.

Before merging, the reviewer should inspect **Files changed**, confirm the change stays within its scope, check that no local credentials or databases are included, and reproduce the relevant behavior when practical. Use **Review changes > Approve** only after checking the work.

## Common problems

| Problem | Action |
|---|---|
| Private repository shows 404 | Sign in to the invited account and confirm the invitation was accepted. |
| No files appear in Changes | Work in the cloned project folder and save an actual change before committing. |
| Commit is not linked to your profile | Check the commit email and add/verify that email on your GitHub account. |
| Pull request says blocked | Check the required review, `verify` result, unresolved conversations, and branch update status. |
| Unrelated histories error | Clone the shared repository into a new folder; do not combine a separately initialized repository with it. Preserve any uncommitted work before copying only the intended changes into the clone. |
