# Test Batch Summary Generation
# Usage: .\test_batch_summary.ps1 [-ForceRegenerate]

param(
    [switch]$ForceRegenerate
)

$baseUrl = "http://localhost:8080/api"
$ErrorActionPreference = "Stop"

Write-Host "========================================"
Write-Host "  Batch Summary Generation Test"
Write-Host "========================================"
Write-Host ""

# Step 1: Login
Write-Host "[1/3] Logging in..." -ForegroundColor Yellow

try {
    $loginData = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginData

    $token = $loginResponse.token
    Write-Host "  [OK] Login successful" -ForegroundColor Green
} catch {
    Write-Host "  [ERROR] Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Check news count
Write-Host ""
Write-Host "[2/3] Checking news..." -ForegroundColor Yellow

try {
    $newsResponse = Invoke-RestMethod -Uri "$baseUrl/news?page=0&size=100" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $token" }

    $totalNews = $newsResponse.totalElements
    Write-Host "  [OK] Found $totalNews news items" -ForegroundColor Green

} catch {
    Write-Host "  [ERROR] Failed to get news: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Start batch generation
Write-Host ""
Write-Host "[3/3] Starting batch summary generation..." -ForegroundColor Yellow

if ($ForceRegenerate) {
    Write-Host "  [INFO] Mode: Force regenerate all" -ForegroundColor Magenta
    $url = "$baseUrl/summaries/generate/batch?forceRegenerate=true"
} else {
    Write-Host "  [INFO] Mode: Generate missing only" -ForegroundColor Cyan
    $url = "$baseUrl/summaries/generate/batch?forceRegenerate=false"
}

try {
    $batchResponse = Invoke-RestMethod -Uri $url `
        -Method Post `
        -Headers @{ Authorization = "Bearer $token" }

    Write-Host "  [OK] $($batchResponse.message)" -ForegroundColor Green
    Write-Host "  [INFO] Status: $($batchResponse.status)" -ForegroundColor Cyan

} catch {
    Write-Host "  [ERROR] Batch generation failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================"
Write-Host "  Batch generation started!"
Write-Host "========================================"
Write-Host ""
Write-Host "[INFO] Processing in background, estimated time: $($totalNews * 3) seconds" -ForegroundColor Cyan
Write-Host ""
Write-Host "Check logs:" -ForegroundColor Yellow
Write-Host "  Get-Content backend\logs\news-management.log -Tail 20 -Wait" -ForegroundColor Gray
Write-Host ""
Write-Host "Re-run test:" -ForegroundColor Yellow
Write-Host "  .\test_batch_summary.ps1" -ForegroundColor Gray
Write-Host "  .\test_batch_summary.ps1 -ForceRegenerate" -ForegroundColor Gray
Write-Host ""

