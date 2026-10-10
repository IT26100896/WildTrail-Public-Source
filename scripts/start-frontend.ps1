param([switch]$Production)
$ErrorActionPreference='Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
if (!(Test-Path -LiteralPath 'node_modules/next')) { npm.cmd ci; if($LASTEXITCODE -ne 0){throw 'Dependency installation failed.'} }
if ($Production) { npm.cmd run build; if($LASTEXITCODE -ne 0){throw 'Frontend build failed.'}; npm.cmd start }
else { npm.cmd run dev }
