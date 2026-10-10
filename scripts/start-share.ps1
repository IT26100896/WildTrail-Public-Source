$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $taskRoot
$taskStatePath=Join-Path $taskRoot '.local/public-share.json'
New-Item -ItemType Directory -Force -Path '.local/share','.tools/cloudflared' | Out-Null
$taskCloud=Join-Path $taskRoot '.tools/cloudflared/cloudflared.exe'
$taskTunnel=$null
if(Test-Path -LiteralPath $taskStatePath){
    $taskOld=Get-Content -LiteralPath $taskStatePath -Raw | ConvertFrom-Json
    $taskRunning=Get-Process -Id $taskOld.tunnelPid -ErrorAction SilentlyContinue
    if($taskRunning -and $taskRunning.Path -eq $taskCloud){
        if((Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue) -and (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue)){
            Write-Output "Already sharing: $($taskOld.url)/client/home";return
        }
        $taskTunnel=$taskRunning
    }
}
if(!(Test-Path -LiteralPath '.local/config.json')){& "$PSScriptRoot/setup.ps1"}
$taskConfig=Get-Content -LiteralPath '.local/config.json' -Raw | ConvertFrom-Json
foreach($taskProperty in $taskConfig.PSObject.Properties){[Environment]::SetEnvironmentVariable($taskProperty.Name,[string]$taskProperty.Value,'Process')}
$taskJava='C:\Program Files\Java\jdk-17\bin\java.exe'
if(!(Test-Path -LiteralPath $taskJava)){throw 'Install Java 17, or update the Java path in this script.'}
if(!(Test-Path -LiteralPath 'backend/target/wildtrail-backend-1.0.0.jar')){throw 'Build the backend first with scripts/start-backend.ps1 -Build.'}
if(!(Test-Path -LiteralPath $taskCloud)){
    $taskRelease=Invoke-RestMethod -Uri 'https://api.github.com/repos/cloudflare/cloudflared/releases/latest'
    $taskAsset=$taskRelease.assets | Where-Object name -eq 'cloudflared-windows-amd64.exe'
    Invoke-WebRequest -Uri $taskAsset.browser_download_url -OutFile $taskCloud
    $taskHash=(Get-FileHash -LiteralPath $taskCloud -Algorithm SHA256).Hash.ToLowerInvariant()
    if(!$taskAsset.digest -or $taskAsset.digest -ne "sha256:$taskHash"){throw 'Cloudflared published SHA256 did not match. Do not run the downloaded binary.'}
}
$taskListener=Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if($taskListener){
    $taskExisting=Get-CimInstance Win32_Process -Filter "ProcessId=$($taskListener.OwningProcess)"
    if($taskExisting.Name -ne 'java.exe' -or $taskExisting.CommandLine -notmatch 'wildtrail-backend-1\.0\.0\.jar'){throw 'Port 8080 belongs to another app. Stop that app before sharing.'}
}
if(!(Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue)){
    if(!(Test-Path -LiteralPath '.next/BUILD_ID')){npm.cmd run build;if($LASTEXITCODE -ne 0){throw 'Frontend build failed.'}}
    Start-Process -FilePath (Get-Command node).Source -ArgumentList 'node_modules/next/dist/bin/next','start' -WorkingDirectory $taskRoot -WindowStyle Hidden -RedirectStandardOutput '.local/share/web.log' -RedirectStandardError '.local/share/web-error.log' | Out-Null
}
if(!$taskTunnel){$taskTunnel=Start-Process -FilePath $taskCloud -ArgumentList 'tunnel','--url','http://127.0.0.1:3000','--protocol','http2','--no-autoupdate' -WorkingDirectory $taskRoot -WindowStyle Hidden -RedirectStandardOutput '.local/share/tunnel.log' -RedirectStandardError '.local/share/tunnel-error.log' -PassThru}
try{
    $taskUrl=$null
    for($taskAttempt=0;$taskAttempt -lt 100;$taskAttempt++){
        $taskLog=Get-Content -LiteralPath '.local/share/tunnel-error.log' -Raw -ErrorAction SilentlyContinue
        if($taskLog -match 'https://[a-z0-9-]+\.trycloudflare\.com'){$taskUrl=$Matches[0];break}
        if($taskTunnel.HasExited){throw 'Cloudflare exited. See .local/share/tunnel-error.log.'}
        Start-Sleep -Milliseconds 500
    }
    if(!$taskUrl){throw 'Cloudflare did not return a URL. See .local/share/tunnel-error.log.'}
    # Allow only this generated origin and local development, never arbitrary domains.
    $env:APP_ORIGINS="http://localhost:3000,http://127.0.0.1:3000,$taskUrl"
    $env:SECURE_COOKIES='true'
    $env:SPRING_DATASOURCE_URL='jdbc:h2:file:./backend/data/wildtrail;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH'
    $env:WILDTRAIL_DATA_DIR='./backend/data'
    if($taskListener){Stop-Process -Id $taskListener.OwningProcess}
    $taskApi=Start-Process -FilePath $taskJava -ArgumentList '-jar','backend/target/wildtrail-backend-1.0.0.jar' -WorkingDirectory $taskRoot -WindowStyle Hidden -RedirectStandardOutput '.local/share/api.log' -RedirectStandardError '.local/share/api-error.log' -PassThru
    $taskReady=$false
    $taskWait=[Diagnostics.Stopwatch]::StartNew()
    while($taskWait.Elapsed.TotalSeconds -lt 120){
        if($taskApi.HasExited){throw 'Java backend exited. See .local/share/api-error.log.'}
        try{
            $taskHealth=Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:8080/api/packages' -TimeoutSec 2
            if($taskHealth.StatusCode -eq 200){$taskReady=$true;break}
        }catch{}
        Start-Sleep -Milliseconds 500
    }
    if(!$taskReady){throw 'Java backend did not become ready. See .local/share/api.log.'}
    $taskHome=Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:3000/client/home' -TimeoutSec 15
    if($taskHome.StatusCode -ne 200){throw 'Local website is not ready.'}
    $taskState=@{url=$taskUrl;tunnelPid=$taskTunnel.Id;apiPid=$taskApi.Id;startedAt=(Get-Date).ToString('o')}
    $taskState | ConvertTo-Json | Set-Content -LiteralPath $taskStatePath -Encoding utf8
    "$taskUrl/client/home" | Set-Content -LiteralPath '.local/public-link.txt' -Encoding utf8
    Write-Output "Public website: $taskUrl/client/home"
    Write-Output 'Keep this PC, the web servers, and Cloudflare running. Stop sharing with scripts/stop-share.ps1.'
}catch{
    if(!$taskTunnel.HasExited){Stop-Process -Id $taskTunnel.Id}
    throw
}


