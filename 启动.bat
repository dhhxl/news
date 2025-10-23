@echo off
chcp 936 > nul
cls
color 0A
echo ========================================
echo    News Management System - Quick Start
echo ========================================
echo.
echo Starting services, please wait...
echo.

echo [1/5] Checking Docker status...
docker --version > nul 2>&1
if errorlevel 1 (
    echo   [WARNING] Docker not installed or not running
    echo   Please start Docker Desktop first
    pause
    exit /b 1
)
echo   [OK] Docker is ready
echo.

echo [2/5] Starting database services (MySQL + Redis)...
docker-compose up -d
if errorlevel 1 (
    echo   [ERROR] Database startup failed
    pause
    exit /b 1
)
echo   [OK] Database services started
echo.

echo [3/5] Waiting for database initialization...
echo   Waiting for MySQL and Redis to be ready...
timeout /t 5 /nobreak > nul
echo   [OK] Database is ready
echo.

echo [4/5] Starting backend service (new window)...
REM Check if ZhipuAI API Key is configured
if "%ZHIPUAI_API_KEY%"=="" (
    echo   [INFO] ZhipuAI API Key not configured, using mock summary
    echo   [INFO] To use real AI summary, set environment variable ZHIPUAI_API_KEY
)
start "News System - Backend Service" cmd /k "cd /d %~dp0backend && echo Starting backend service... && mvn spring-boot:run"
timeout /t 3 /nobreak > nul
echo   [OK] Backend service starting...
echo.

echo [5/5] Starting frontend service (new window)...
start "News System - Frontend Service" cmd /k "cd /d %~dp0frontend && echo Starting frontend service... && npm run dev"
echo   [OK] Frontend service starting...
echo.

echo ========================================
echo    Startup Complete!
echo ========================================
echo.
echo [WAIT TIME] Please wait 30-40 seconds for all services to fully start
echo.
echo ========================================
echo    Access URLs
echo ========================================
echo.
echo  Frontend (User)
echo   Home:        http://localhost:5173
echo   News Detail: http://localhost:5173/news/[id]
echo   Category:    http://localhost:5173/category/[id]
echo.
echo  Backend (Admin)
echo   Home:        http://localhost:5173/admin
echo   Login:       http://localhost:5173/login
echo   Dashboard:   http://localhost:5173/admin
echo   News Mgmt:   http://localhost:5173/admin/news
echo   Crawler:     http://localhost:5173/admin/crawler
echo   Categories:  http://localhost:5173/admin/categories
echo.
echo  Backend API
echo   API Base:    http://localhost:8080/api
echo   Health:      http://localhost:8080/api/health
echo   News List:   http://localhost:8080/api/news
echo   Categories:  http://localhost:8080/api/categories
echo.
echo ========================================
echo    Login Information
echo ========================================
echo.
echo   Username: admin
echo   Password: admin123
echo.
echo ========================================
echo    Service Verification
echo ========================================
echo.
echo Waiting 30 seconds before checking service status...
timeout /t 30 /nobreak > nul
echo.
echo Checking service status...
echo.

REM Check MySQL
netstat -an | findstr ":3306" > nul 2>&1
if errorlevel 1 (
    echo [X] MySQL     Port 3306 - Not running
) else (
    echo [OK] MySQL    Port 3306 - Running
)

REM Check Redis
netstat -an | findstr ":6379" > nul 2>&1
if errorlevel 1 (
    echo [X] Redis     Port 6379 - Not running
) else (
    echo [OK] Redis    Port 6379 - Running
)

REM Check Backend
netstat -an | findstr ":8080" > nul 2>&1
if errorlevel 1 (
    echo [X] Backend   Port 8080 - Not running (may still be starting)
    echo     Please wait or check backend window logs
) else (
    echo [OK] Backend  Port 8080 - Running
)

REM Check Frontend
netstat -an | findstr ":5173" > nul 2>&1
if errorlevel 1 (
    echo [X] Frontend  Port 5173 - Not running (may still be starting)
    echo     Please wait or check frontend window logs
) else (
    echo [OK] Frontend Port 5173 - Running
)

echo.
echo ========================================
echo    Quick Actions
echo ========================================
echo.
echo  1. Check detailed status: run check_status.bat
echo  2. Stop all services: run stop_system.bat
echo  3. For help: check README.md
echo.
echo ========================================
echo    Tips
echo ========================================
echo.
echo  - If services not running, check corresponding window logs
echo  - Backend startup takes 20-30 seconds
echo  - Frontend startup takes 5-10 seconds
echo  - You can close this window, services will continue running
echo.
echo ========================================
pause
