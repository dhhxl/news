# Initialize Test Data
# Crawl news and generate summaries

$baseUrl = "http://localhost:8080/api"

Write-Host "========================================"
Write-Host "  Initialize Test Data"
Write-Host "========================================"
Write-Host ""

# Login as admin
Write-Host "[1/3] Logging in as admin..." -ForegroundColor Yellow

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

Write-Host ""

# Trigger crawler
Write-Host "[2/3] Starting news crawler..." -ForegroundColor Yellow

try {
    $crawlResponse = Invoke-RestMethod -Uri "$baseUrl/crawler/trigger/all?maxCount=5" `
        -Method Post `
        -Headers @{ Authorization = "Bearer $token" }

    Write-Host "  [OK] Crawler started" -ForegroundColor Green
    Write-Host "  [INFO] Fetching 5 news from each source..." -ForegroundColor Cyan
} catch {
    Write-Host "  [ERROR] Crawler failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  [INFO] This is optional, you can continue without crawled news" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "  [INFO] Waiting 30 seconds for crawler to complete..." -ForegroundColor Cyan
Start-Sleep -Seconds 30

# Generate summaries
Write-Host ""
Write-Host "[3/3] Generating summaries..." -ForegroundColor Yellow

try {
    $summaryResponse = Invoke-RestMethod -Uri "$baseUrl/summaries/generate/batch" `
        -Method Post `
        -Headers @{ Authorization = "Bearer $token" }

    Write-Host "  [OK] $($summaryResponse.message)" -ForegroundColor Green
} catch {
    Write-Host "  [WARNING] Summary generation failed (this is optional)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================"
Write-Host "  Data Initialization Complete!"
Write-Host "========================================"
Write-Host ""

# Check news count
try {
    $newsResponse = Invoke-RestMethod -Uri "$baseUrl/news?page=0&size=100" `
        -Method Get

    $count = $newsResponse.totalElements
    Write-Host "Total news in database: $count" -ForegroundColor Green
} catch {
    Write-Host "Failed to check news count" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "You can now:" -ForegroundColor Cyan
Write-Host "  1. Visit: http://localhost:5173" -ForegroundColor White
Write-Host "  2. Register a new user" -ForegroundColor White
Write-Host "  3. Like and comment on news" -ForegroundColor White
Write-Host ""

