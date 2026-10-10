$ErrorActionPreference='Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
$env:MAVEN_OPTS="$env:MAVEN_OPTS -Djava.net.preferIPv4Stack=true"
& 'backend/mvnw.cmd' -f backend/pom.xml test
if($LASTEXITCODE -ne 0){throw 'Java tests failed.'}
foreach($taskCheck in @('lint','build','typecheck')) {
    npm.cmd run $taskCheck
    if($LASTEXITCODE -ne 0){throw "Frontend $taskCheck failed."}
}
