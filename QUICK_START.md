# Quick Start - Fix Timeout Issue

## Problem
Batch summary generation causes other pages to timeout (30 seconds).

## Solution Applied
1. ✅ Added async task thread pool configuration
2. ✅ Optimized batch processing logic
3. ✅ Increased frontend timeout to 60 seconds

## Quick Fix Steps

### Step 1: Restart Backend

```batch
cd E:\毕业设计\News_website
.\restart_backend.bat
```

Wait for backend to start (see "Started NewsManagementApplication" in the new window).

### Step 2: Verify Fix

```powershell
.\verify_fix.ps1
```

Expected output:
```
[OK] Backend is running
[OK] Login successful
[OK] News list loaded
[OK] Response time: 0.5 seconds
[OK] Categories loaded
All tests passed!
```

### Step 3: Test Batch Summary (Optional)

```powershell
# Generate missing summaries only
.\test_batch_summary.ps1

# Force regenerate all summaries
.\test_batch_summary.ps1 -ForceRegenerate
```

**Important Test**: While batch generation is running, open http://localhost:5173 in your browser. All pages should load normally (no timeout).

## Command Reference

```batch
# Restart backend
.\restart_backend.bat

# Verify fix
.\verify_fix.ps1

# Test batch summary
.\test_batch_summary.ps1
.\test_batch_summary.ps1 -ForceRegenerate

# View logs
Get-Content backend\logs\news-management.log -Tail 20 -Wait

# Check services
docker ps
netstat -an | findstr ":8080"
```

## What Changed

### Backend Changes

1. **Added**: `backend/src/main/java/com/news/config/AsyncConfig.java`
   - Thread pool: 2-5 threads
   - Queue capacity: 100
   - Prevents resource exhaustion

2. **Modified**: `backend/src/main/java/com/news/service/SummaryService.java`
   - Paged processing (20 items per page)
   - 3 second delay per summary
   - Better error handling

3. **Modified**: `backend/src/main/java/com/news/controller/SummaryController.java`
   - Added `forceRegenerate` parameter

### Frontend Changes

1. **Modified**: `frontend/src/utils/request.ts`
   - Timeout: 30s → 60s

## Expected Performance

| Scenario | Before | After |
|----------|--------|-------|
| Homepage while batch running | ❌ Timeout 30s | ✅ Load < 2s |
| Admin while batch running | ❌ Timeout 30s | ✅ Load < 3s |
| News detail while batch running | ❌ Timeout 30s | ✅ Load < 1s |
| Batch execution time | Unknown | ~3s per item |
| System resource usage | ⚠️ May exhaust | ✅ Controlled (2-5 threads) |

## Troubleshooting

### Issue: Still timing out

1. **Check backend restart**:
   ```powershell
   Get-Content backend\logs\news-management.log -Tail 5
   # Should see: "Async task executor initialized"
   ```

2. **Check database**:
   ```powershell
   docker ps
   # Should see: news_mysql and news_redis running
   ```

3. **Check errors**:
   ```powershell
   Get-Content backend\logs\news-management.log -Tail 100 | Select-String "ERROR"
   ```

### Issue: Batch generation not working

1. **Check published news**:
   - Login to admin panel
   - Go to news management
   - Ensure some news are in "PUBLISHED" status

2. **Check logs**:
   ```powershell
   Get-Content backend\logs\news-management.log -Tail 50 | Select-String "batch"
   ```

3. **Try single generation**:
   - In admin panel, click "Generate Summary" on a single news item
   - If it works, batch should work too

### Issue: Mock summaries instead of real AI

This is **normal** if you haven't configured ZhipuAI API Key. The system automatically falls back to mock summaries.

**To fix**: Run `.\配置API密钥.bat` and enter your API key.

## Files Created

### Core Fix
- `backend/src/main/java/com/news/config/AsyncConfig.java` - Async task configuration

### Helper Scripts (English filenames to avoid encoding issues)
- `restart_backend.bat` - Restart backend service
- `verify_fix.ps1` - Verify timeout fix
- `test_batch_summary.ps1` - Test batch summary generation
- `QUICK_START.md` - This file

### Documentation (Chinese filenames)
- `批量生成摘要超时问题修复.md` - Detailed fix explanation
- `批量生成摘要改进说明.md` - Feature improvement explanation

## Support

If you need help:
1. Check backend logs: `Get-Content backend\logs\news-management.log -Tail 50`
2. Check browser console (F12)
3. Review detailed documentation in Chinese files

---

**Start here**: `.\restart_backend.bat` then `.\verify_fix.ps1`

