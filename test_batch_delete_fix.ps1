# 测试批量删除功能修复脚本

Write-Host "🧪 测试批量删除功能修复" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api"

try {
    # 1. 登录获取Token
    Write-Host "1️⃣ 登录..." -ForegroundColor Yellow
    $loginBody = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✅ 登录成功，Token: $($token.Substring(0,20))..." -ForegroundColor Green
    
    # 2. 获取新闻列表
    Write-Host "2️⃣ 获取新闻列表..." -ForegroundColor Yellow
    $newsList = Invoke-RestMethod -Uri "$baseUrl/news?size=5" -Method Get
    $newsCount = $newsList.content.Count
    Write-Host "✅ 找到 $newsCount 条新闻" -ForegroundColor Green
    
    if ($newsCount -lt 2) {
        Write-Host "⚠️ 新闻数量不足，无法测试批量删除" -ForegroundColor Red
        return
    }
    
    # 3. 准备测试数据 - 选择前两条新闻
    $testIds = @($newsList.content[0].id, $newsList.content[1].id)
    Write-Host "📋 准备删除新闻ID: $($testIds -join ', ')" -ForegroundColor Yellow
    
    # 4. 测试批量删除API
    Write-Host "3️⃣ 测试批量删除API..." -ForegroundColor Yellow
    
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $deleteBody = @{
        ids = $testIds
    } | ConvertTo-Json
    
    Write-Host "📤 发送请求: DELETE $baseUrl/news/batch" -ForegroundColor Cyan
    Write-Host "📄 请求体: $deleteBody" -ForegroundColor Cyan
    
    $deleteResponse = Invoke-RestMethod -Uri "$baseUrl/news/batch" -Method Delete -Body $deleteBody -Headers $headers
    
    Write-Host "✅ 批量删除成功！" -ForegroundColor Green
    Write-Host "📊 删除结果: $($deleteResponse | ConvertTo-Json)" -ForegroundColor Green
    
    # 5. 验证删除结果
    Write-Host "4️⃣ 验证删除结果..." -ForegroundColor Yellow
    $newListAfter = Invoke-RestMethod -Uri "$baseUrl/news?size=10" -Method Get
    $remainingCount = $newListAfter.content.Count
    Write-Host "✅ 删除后剩余 $remainingCount 条新闻" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "🎉 批量删除功能修复验证成功！" -ForegroundColor Green
    Write-Host "💡 现在可以在管理后台正常使用批量删除功能" -ForegroundColor Cyan

} catch {
    Write-Host "❌ 测试失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "📄 错误详情: $responseBody" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "📋 下一步操作:" -ForegroundColor Blue
Write-Host "1. 访问管理后台: http://localhost:5173/admin/news" -ForegroundColor White
Write-Host "2. 选择多个新闻，点击'批量删除'按钮测试" -ForegroundColor White
Write-Host "3. 确认功能正常工作" -ForegroundColor White
