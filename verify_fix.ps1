# Verify Timeout Fix
# Usage: .\verify_fix.ps1

$baseUrl = "http://localhost:8080/api"

Write-Host "========================================"
Write-Host "  Verify Timeout Fix"
Write-Host "========================================"
Write-Host ""

# Check backend
Write-Host "[1/4] Checking backend status..." -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/actuator/health" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "  [OK] Backend is running" -ForegroundColor Green
} catch {
    Write-Host "  [ERROR] Backend is not running" -ForegroundColor Red
    Write-Host "  [INFO] Please run: .\restart_backend.bat" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Test login
Write-Host "[2/4] Testing login..." -ForegroundColor Yellow

try {
    $loginData = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json

    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginData `
        -TimeoutSec 10

    $token = $loginResponse.token
    Write-Host "  [OK] Login successful" -ForegroundColor Green
} catch {
    Write-Host "  [ERROR] Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Test news list
Write-Host "[3/4] Testing news list..." -ForegroundColor Yellow

try {
    $startTime = Get-Date
    
    $newsResponse = Invoke-RestMethod -Uri "$baseUrl/news?page=0&size=20" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $token" } `
        -TimeoutSec 60

    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds

    Write-Host "  [OK] News list loaded" -ForegroundColor Green
    Write-Host "  [INFO] Response time: $([math]::Round($duration, 2)) seconds" -ForegroundColor Cyan
    
    if ($duration -gt 5) {
        Write-Host "  [WARNING] Response time is slow" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  [ERROR] News list failed: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Message -like "*timeout*") {
        Write-Host "  [ERROR] Still timing out!" -ForegroundColor Red
        Write-Host "  [INFO] Check backend logs:" -ForegroundColor Yellow
        Write-Host "    Get-Content backend\logs\news-management.log -Tail 50" -ForegroundColor Gray
    }
    exit 1
}

Write-Host ""

# Test categories
Write-Host "[4/4] Testing categories..." -ForegroundColor Yellow

try {
    $startTime = Get-Date
    
    $catResponse = Invoke-RestMethod -Uri "$baseUrl/categories" `
        -Method Get `
        -Headers @{ Authorization = "Bearer $token" } `
        -TimeoutSec 60

    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds

    Write-Host "  [OK] Categories loaded" -ForegroundColor Green
    Write-Host "  [INFO] Response time: $([math]::Round($duration, 2)) seconds" -ForegroundColor Cyan
} catch {
    Write-Host "  [ERROR] Categories failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================"
Write-Host "  All tests passed!" -ForegroundColor Green
Write-Host "========================================"
Write-Host ""

Write-Host "Timeout issue is fixed! You can now:" -ForegroundColor Green
Write-Host "  1. Visit frontend: http://localhost:5173" -ForegroundColor White
Write-Host "  2. Visit admin: http://localhost:5173/admin" -ForegroundColor White
Write-Host "  3. Test batch summary:" -ForegroundColor White
Write-Host "     .\test_batch_summary.ps1" -ForegroundColor Gray
Write-Host ""

