param([switch]$Build)
$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $taskRoot
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { throw 'Port 8080 is already in use. Stop the existing API before starting or rebuilding it.' }
if (!$env:JAVA_HOME -and (Test-Path -LiteralPath 'C:\Program Files\Java\jdk-17')) { $env:JAVA_HOME='C:\Program Files\Java\jdk-17' }
$env:MAVEN_OPTS="$env:MAVEN_OPTS -Djava.net.preferIPv4Stack=true"
if (!(Test-Path -LiteralPath '.local/config.json')) { & "$PSScriptRoot/setup.ps1" }
$taskConfig=Get-Content -LiteralPath '.local/config.json' -Raw | ConvertFrom-Json
foreach ($taskProperty in $taskConfig.PSObject.Properties) { [Environment]::SetEnvironmentVariable($taskProperty.Name,[string]$taskProperty.Value,'Process') }
$taskJava=if($env:JAVA_HOME){Join-Path $env:JAVA_HOME 'bin/java.exe'}else{''}
if (!(Test-Path -LiteralPath $taskJava)) { $taskJava=(Get-Command java -ErrorAction Stop).Source }
if ($Build -or !(Test-Path -LiteralPath 'backend/target/wildtrail-backend-1.0.0.jar')) {
    $taskMaven=Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($taskMaven) { & $taskMaven.Source -f backend/pom.xml clean package }
    elseif (Test-Path -LiteralPath 'backend/mvnw.cmd') { & 'backend/mvnw.cmd' -f backend/pom.xml clean package }
    elseif (Test-Path -LiteralPath '.tools/apache-maven-3.9.11/bin/mvn.cmd') { & '.tools/apache-maven-3.9.11/bin/mvn.cmd' -f backend/pom.xml clean package }
    else { throw 'Install Maven 3.9+ and use Java 17+, then rerun this script.' }
    if ($LASTEXITCODE -ne 0) { throw 'Java build failed.' }
}
& $taskJava -jar backend/target/wildtrail-backend-1.0.0.jar
