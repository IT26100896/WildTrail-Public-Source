$ErrorActionPreference='Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
# Stop the normal API first. This database is reserved for sample browser QA.
$env:SPRING_DATASOURCE_URL='jdbc:h2:file:./.local/browser-qa/database;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE'
$env:WILDTRAIL_DATA_DIR='./.local/browser-qa/files'
& "$PSScriptRoot/start-backend.ps1"
