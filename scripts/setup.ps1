param([string]$AdminEmail = 'staff@wildtrail.local')
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $taskRoot
if (Test-Path -LiteralPath '.local/config.json') { Write-Host 'Local configuration already exists. Existing accounts and data were preserved.'; exit 0 }
if ($AdminEmail -notmatch '^[^\s@]+@[^\s@]+\.[^\s@]+$') { throw 'Provide a valid administrator email.' }
New-Item -ItemType Directory -Force -Path '.local' | Out-Null
$taskRng = [Security.Cryptography.RandomNumberGenerator]::Create()
$taskSecretBytes = New-Object byte[] 48
$taskPasswordBytes = New-Object byte[] 24
$taskRng.GetBytes($taskSecretBytes)
$taskRng.GetBytes($taskPasswordBytes)
$taskRng.Dispose()
$taskSecret = [Convert]::ToBase64String($taskSecretBytes)
$taskPassword = [Convert]::ToBase64String($taskPasswordBytes) + '!aA7'
@{ SESSION_SECRET=$taskSecret; ADMIN_EMAIL=$AdminEmail; ADMIN_PASSWORD=$taskPassword } | ConvertTo-Json | Set-Content -LiteralPath '.local/config.json' -Encoding utf8
@"
WildTrail local administrator access
URL: http://localhost:3000/admin/login
Email: $AdminEmail
Password: $taskPassword

Keep this file private. It is excluded from Git and the handoff ZIP.
Configuration provisions an administrator only if none exists; it never resets existing passwords.
"@ | Set-Content -LiteralPath '.local/admin-access.txt' -Encoding utf8
Write-Host 'Created local configuration. Your administrator credentials are in .local/admin-access.txt.'
