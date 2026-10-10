$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
$taskStatePath=Join-Path $taskRoot '.local/public-share.json'
if(!(Test-Path -LiteralPath $taskStatePath)){Write-Output 'No saved sharing session.';return}
$taskState=Get-Content -LiteralPath $taskStatePath -Raw | ConvertFrom-Json
$taskProcess=Get-Process -Id $taskState.tunnelPid -ErrorAction SilentlyContinue
$taskExpected=Join-Path $taskRoot '.tools/cloudflared/cloudflared.exe'
if($taskProcess -and $taskProcess.Path -eq $taskExpected){Stop-Process -Id $taskProcess.Id}
Remove-Item -LiteralPath $taskStatePath -Force
$taskLink=Join-Path $taskRoot '.local/public-link.txt'
if(Test-Path -LiteralPath $taskLink){Remove-Item -LiteralPath $taskLink -Force}
Write-Output 'Public sharing stopped. The local website is still running.'
