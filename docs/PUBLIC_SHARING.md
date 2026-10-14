# Cloudflare public sharing

Quick Tunnel shares the local Next.js website through HTTPS without a Cloudflare account, payment card, or purchased domain. Java, H2 and the guide/report files continue running and storing data on this PC.

From the project directory in PowerShell:

```powershell
./scripts/start-share.ps1
```

The script downloads the official Windows x64 cloudflared binary when absent and verifies the release asset's published SHA256. It starts the production frontend when needed, creates a Quick Tunnel using HTTP/2 over IPv4, and restarts this project's Java backend with the exact generated origin allowed and Secure, HttpOnly session cookies. It uses the normal local database and existing private admin configuration. Do not run a second backend on port 8080 while sharing.

The current URL is printed and saved privately in `.local/public-link.txt`. Before reporting success, the script checks a public registration request with the exact tunnel `Origin` header. An empty form must reach Java validation; merely loading the home page does not prove that registration works. Running the script again returns the same active URL only after this check passes. If the backend was restarted with local-only origins, the script configures it again for the active tunnel. No password or session secret is placed in the URL or source files.

Stop public sharing:

```powershell
./scripts/stop-share.ps1
```

This stops only the saved project's cloudflared process, checking its executable path before stopping it. Local web servers remain running. Start sharing again to obtain a new URL; the backend origin configuration is updated for that URL automatically.

Keep the PC awake, internet connected, and all three processes running. The link stops working if the PC sleeps, shuts down, loses internet, or the tunnel stops. Each new Quick Tunnel gets a different hostname. This is a temporary campus demo link, with no uptime guarantee. Share the website URL with teammates; give staff credentials only to intended administrators. The existing website login and admin authorization still apply.

Prerequisites: Windows x64, Java 17 (`JAVA_HOME`, or the default installation path), installed Node dependencies, a built backend JAR, and internet access. On another machine, run the README setup/build steps first. Detailed tunnel/API logs and PID state stay under `.local/` and are excluded from Git and the handoff ZIP. No Windows service or automatic startup task is installed.

## Registration from another device

Open the current saved URL on the other device. A new tunnel has a new hostname, so an older link or QR code may have expired. If local registration works but the public form reports `Invalid CORS request` or `Request failed (403)`, rerun `scripts/start-share.ps1`. It restarts this project's backend with the current public origin. Keep origin restrictions enabled; do not allow every website or disable the guard to make sharing work.

Run the public browser check against disposable demo data:

```powershell
$env:QA_ALLOW_MUTATIONS = '1'
npm run qa:public-registration
```

The check submits the real registration form, checks its session and logout/login, verifies that an unrelated origin remains blocked, and removes its temporary customer. To test a separately started demo, set `QA_BASE_URL` to its current public URL first. The test prints no passwords or session tokens.

Official documentation: https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/
