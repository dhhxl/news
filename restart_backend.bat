@echo off
chcp 65001 > nul
cls
color 0E
echo ========================================
echo    Restart Backend Service
echo ========================================
echo.

echo [1/3] Stopping existing backend process...
echo.

REM Find and kill Java processes
for /f "tokens=2" %%a in ('tasklist /FI "IMAGENAME eq java.exe" /NH 2^>nul ^| findstr "java.exe"') do (
    echo   Found Java process: %%a
    taskkill /F /PID %%a >nul 2>&1
)

timeout /t 2 /nobreak > nul
echo   [OK] Existing processes stopped
echo.

echo [2/3] Starting backend service...
echo   [INFO] Backend will start in a new window
echo   [INFO] Please wait for "Started NewsManagementApplication"
echo.

start "Backend Service" cmd /k "cd /d %~dp0backend && echo Starting backend... && mvn clean compile spring-boot:run"

echo   [INFO] Backend is starting, please wait 30 seconds...
timeout /t 30 /nobreak > nul

echo.
echo [3/3] Checking backend status...
echo.

REM Check if port 8080 is listening
netstat -an | findstr ":8080" > nul 2>&1
if errorlevel 1 (
    echo   [ERROR] Backend is not running on port 8080
    echo   [INFO] Please check the backend window for errors
    echo.
) else (
    echo   [OK] Backend is running on port 8080
    echo.
    
    echo ========================================
    echo    Next Steps
    echo ========================================
    echo.
    echo 1. Verify fix:
    echo    .\verify_fix.ps1
    echo.
    echo 2. Test batch summary generation:
    echo    .\test_batch_summary.ps1
    echo.
    echo 3. Test batch summary with force regenerate:
    echo    .\test_batch_summary.ps1 -ForceRegenerate
    echo.
    echo 4. View logs:
    echo    Get-Content backend\logs\news-management.log -Tail 20 -Wait
    echo.
)

echo ========================================
pause

