param(
    [switch]$ForceStopLocalMySql,
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Set-Location $repoRoot

function Wait-HttpOk {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url,
        [int]$RetryCount = 60,
        [int]$DelaySeconds = 5,
        [string]$Method = "GET"
    )

    for ($i = 0; $i -lt $RetryCount; $i++) {
        try {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -UseBasicParsing -TimeoutSec 20
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300) {
                return $response
            }
        } catch {
        }
        Start-Sleep -Seconds $DelaySeconds
    }

    throw "Timed out waiting for $Url"
}

Write-Host "Repository: $repoRoot" -ForegroundColor Cyan

$listener = Get-NetTCPConnection -LocalPort 3307 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($listener) {
    $process = Get-Process -Id $listener.OwningProcess -ErrorAction Stop
    if ($ForceStopLocalMySql -and $process.ProcessName -ieq 'mysqld') {
        Write-Host "Stopping local mysqld process on port 3307 (PID=$($process.Id))..." -ForegroundColor Yellow
        Stop-Process -Id $process.Id -Force
        Start-Sleep -Seconds 3
    } else {
        throw "Port 3307 is occupied by $($process.ProcessName) (PID=$($process.Id)). Re-run this script in an Administrator PowerShell with -ForceStopLocalMySql, or free the port manually first."
    }
}

if (-not $SkipBuild) {
    Write-Host "Building backend images..." -ForegroundColor Cyan
    docker compose build mall-admin mall-auth mall-gateway mall-monitor mall-portal
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose build failed."
    }
}

Write-Host "Starting backend stack..." -ForegroundColor Cyan
docker compose up -d
if ($LASTEXITCODE -ne 0) {
    throw "docker compose up failed."
}

Write-Host "Waiting for backend services to become ready..." -ForegroundColor Cyan
$healthUrls = @(
    "http://localhost:8080/actuator/health",
    "http://localhost:8401/actuator/health",
    "http://localhost:8201/actuator/health",
    "http://localhost:8101/actuator/health",
    "http://localhost:8085/actuator/health"
)

foreach ($url in $healthUrls) {
    $response = Wait-HttpOk -Url $url
    Write-Host "OK  $url" -ForegroundColor Green
    Write-Output $response.Content
}

$loginUrl = "http://localhost:8201/mall-auth/auth/login?clientId=admin-app&username=admin&password=macro123"
$loginResponse = Wait-HttpOk -Url $loginUrl -Method POST
Write-Host "OK  Auth login through gateway" -ForegroundColor Green
Write-Output $loginResponse.Content

$portalUrls = @(
    "http://localhost:8201/mall-portal/home/content",
    "http://localhost:8201/mall-portal/home/productCateList/0"
)

foreach ($url in $portalUrls) {
    $response = Wait-HttpOk -Url $url
    Write-Host "OK  $url" -ForegroundColor Green
    Write-Output $response.Content
}

Write-Host ""
Write-Host "Deployment completed." -ForegroundColor Green
Write-Host "Open Docker Desktop -> Images -> Local to view the backend images." -ForegroundColor Green
Write-Host "Open Docker Desktop -> Containers to start/stop the mall-backend app group." -ForegroundColor Green
