$ErrorActionPreference='Stop'
$taskRoot=Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $taskRoot
$taskStatePath=Join-Path $taskRoot '.local/public-share.json'
New-Item -ItemType Directory -Force -Path '.local/share','.tools/cloudflared' | Out-Null
$taskCloud=Join-Path $taskRoot '.tools/cloudflared/cloudflared.exe'
$taskTunnel=$null
$taskApi=$null
$taskWeb=$null

function Test-RegistrationOrigin([string]$taskOrigin) {
    # An empty form must reach Java validation (400), rather than CORS rejection.
    # This probe does not create a customer or collect any credentials.
    Add-Type -AssemblyName System.Net.Http
    $taskClient=[System.Net.Http.HttpClient]::new()
    $taskClient.Timeout=[TimeSpan]::FromSeconds(8)
    $taskRequest=[System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post,"$taskOrigin/api/auth/register")
    $taskRequest.Headers.Add('Origin',$taskOrigin)
    $taskRequest.Content=[System.Net.Http.StringContent]::new('{}',[System.Text.Encoding]::UTF8,'application/json')
    $taskResponse=$null
    try {
        $taskResponse=$taskClient.SendAsync($taskRequest).GetAwaiter().GetResult()
        if([int]$taskResponse.StatusCode -ne 400 -or !$taskResponse.Headers.Contains('Access-Control-Allow-Origin')){return $false}
        $taskAllowed=$taskResponse.Headers.GetValues('Access-Control-Allow-Origin') | Select-Object -First 1
        return $taskAllowed -eq $taskOrigin
    } catch { return $false }
    finally {
        if($taskResponse){$taskResponse.Dispose()}
        $taskRequest.Dispose()
        $taskClient.Dispose()
    }
}

if(Test-Path -LiteralPath $taskStatePath){
    $taskOld=Get-Content -LiteralPath $taskStatePath -Raw | ConvertFrom-Json
    $taskRunning=Get-Process -Id $taskOld.tunnelPid -ErrorAction SilentlyContinue
    if($taskRunning -and $taskRunning.Path -eq $taskCloud){
        if((Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue) -and (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) -and (Test-RegistrationOrigin $taskOld.url)){
            Write-Output "Already sharing: $($taskOld.url)/client/home";return
        }
        $taskTunnel=$taskRunning
    }
}
if(!(Test-Path -LiteralPath '.local/config.json')){& "$PSScriptRoot/setup.ps1"}
$taskConfig=Get-Content -LiteralPath '.local/config.json' -Raw | ConvertFrom-Json
foreach($taskProperty in $taskConfig.PSObject.Properties){[Environment]::SetEnvironmentVariable($taskProperty.Name,[string]$taskProperty.Value,'Process')}
$taskJavaCandidates=@()
if($env:JAVA_HOME){$taskJavaCandidates+=Join-Path $env:JAVA_HOME 'bin/java.exe'}
$taskJavaCandidates+='C:\Program Files\Java\jdk-17\bin\java.exe'
$taskJava=$taskJavaCandidates | Where-Object {Test-Path -LiteralPath $_} | Select-Object -First 1
if(!$taskJava){throw 'Install Java 17 and set JAVA_HOME to its installation directory.'}
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
    if($taskExisting.ExecutablePath -ne $taskJava -or $taskExisting.CommandLine -notmatch 'wildtrail-backend-1\.0\.0\.jar'){throw 'Port 8080 belongs to another app. Stop that app before sharing.'}
    $taskJcmd=Join-Path (Split-Path $taskJava -Parent) 'jcmd.exe'
    $taskProperties=& $taskJcmd $taskExisting.ProcessId VM.system_properties 2>$null
    $taskDirectory=$taskProperties | Where-Object {$_ -like 'user.dir=*'} | Select-Object -First 1
    # jcmd prints Windows paths in Java-properties format (F\:\\folder).
    $taskBackendRoot=if($taskDirectory){$taskDirectory.Substring(9).Replace('\:',':').Replace('\\','\')}else{''}
    if($taskBackendRoot.TrimEnd('\') -ne $taskRoot.TrimEnd('\')){throw 'Port 8080 belongs to a different checkout. Stop that backend before sharing.'}
}
if(!(Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue)){
    if(!(Test-Path -LiteralPath '.next/BUILD_ID')){npm.cmd run build;if($LASTEXITCODE -ne 0){throw 'Frontend build failed.'}}
    $taskWeb=Start-Process -FilePath (Get-Command node).Source -ArgumentList 'node_modules/next/dist/bin/next','start' -WorkingDirectory $taskRoot -WindowStyle Hidden -RedirectStandardOutput '.local/share/web.log' -RedirectStandardError '.local/share/web-error.log' -PassThru
}
if(!$taskTunnel){$taskTunnel=Start-Process -FilePath $taskCloud -ArgumentList 'tunnel','--url','http://127.0.0.1:3000','--protocol','http2','--edge-ip-version','4','--no-autoupdate' -WorkingDirectory $taskRoot -WindowStyle Hidden -RedirectStandardOutput '.local/share/tunnel.log' -RedirectStandardError '.local/share/tunnel-error.log' -PassThru}
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
    if($taskListener){
        Stop-Process -Id $taskListener.OwningProcess
        for($taskAttempt=0;$taskAttempt -lt 20 -and (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue);$taskAttempt++){Start-Sleep -Milliseconds 500}
        if(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue){throw 'The previous backend did not release port 8080.'}
    }
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
    $taskPublicReady=$false
    for($taskAttempt=0;$taskAttempt -lt 5;$taskAttempt++){
        if(Test-RegistrationOrigin $taskUrl){$taskPublicReady=$true;break}
        Start-Sleep -Seconds 1
    }
    if(!$taskPublicReady){throw 'Public registration is unavailable. The backend must allow the current tunnel origin. See .local/share logs.'}
    $taskState=@{url=$taskUrl;tunnelPid=$taskTunnel.Id;apiPid=$taskApi.Id;startedAt=(Get-Date).ToString('o')}
    $taskState | ConvertTo-Json | Set-Content -LiteralPath $taskStatePath -Encoding utf8
    "$taskUrl/client/home" | Set-Content -LiteralPath '.local/public-link.txt' -Encoding utf8
    Write-Output "Public website: $taskUrl/client/home"
    Write-Output 'Keep this PC, the web servers, and Cloudflare running. Stop sharing with scripts/stop-share.ps1.'
}catch{
    if(!$taskTunnel.HasExited){Stop-Process -Id $taskTunnel.Id}
    if($taskApi -and !$taskApi.HasExited){Stop-Process -Id $taskApi.Id}
    if($taskWeb -and !$taskWeb.HasExited){Stop-Process -Id $taskWeb.Id}
    throw
}


