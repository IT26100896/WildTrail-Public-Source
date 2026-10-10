# GitHub Desktop workflow for all six members

Group **26-MTR-Y1S2-WD-009** uses one private GitHub repository. The project owner publishes the integrated baseline once. Each invited student accepts the email invitation to their own GitHub account. Student mail domain is `@my.sliit.lk`.

## Owner: initial upload

1. In GitHub Desktop, sign in as `IT26100896`. Choose **File > Add local repository** and select the folder containing this `README.md` (the `.git` folder is already prepared locally).
2. Check the changed-files list. The source must not include `.local/`, `.env*`, `node_modules/`, `.next/`, `backend/data/`, `backend/target/` or real account data.
3. The private repository is `IT26100896/WildTrail-Public-Source`. Confirm that `origin` points to it and push `main`. If starting from a fresh copy without a remote, publish under the owner account with **Keep this code private** selected.
4. On GitHub open **Settings > Collaborators > Add people**. Invite the other five students via their exact `@my.sliit.lk` emails. They must accept. This requires owner/admin permission.
5. Turn on pull request review and branch protection if available, so `main` changes are reviewed before merge. Do not grant an administrator role unless it is required.

## Every member: actual contribution

1. Accept your invite, log in to GitHub Desktop with your own student account, then **File > Clone repository**. Never use another member's login.
2. Choose **Branch > New branch** using `member/it26100xxx-module`; see your `team/IT.../README.md` for the exact suggestion.
3. Open `backend/pom.xml` in IntelliJ IDEA. Review your Java module and its UI routes, make your own improvement or fix, and run the relevant checks.
4. Return to GitHub Desktop, inspect the changed files and diff, write an accurate commit summary, click **Commit to branch**, then **Push origin**.
5. Click **Create Pull Request**. The owner/team reviews it and merges only after checks pass. Keep each person's real commits in the history for the final report and viva.

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
