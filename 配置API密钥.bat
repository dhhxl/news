@echo off
chcp 65001 > nul
cls
color 0B
echo ========================================
echo    配置 ZhipuAI API Key
echo ========================================
echo.
echo 说明：
echo  1. 访问 https://open.bigmodel.cn/
echo  2. 注册/登录账号
echo  3. 获取 API Key
echo  4. 输入您的 API Key 到下方
echo.
echo ========================================
echo.

set /p API_KEY="请输入您的 ZhipuAI API Key: "

if "%API_KEY%"=="" (
    echo.
    echo [错误] API Key 不能为空
    pause
    exit /b 1
)

echo.
echo ========================================
echo    配置方式选择
echo ========================================
echo.
echo  1. 临时配置（仅本次会话有效）
echo  2. 永久配置（添加到系统环境变量）
echo.

set /p CHOICE="请选择 (1 或 2): "

if "%CHOICE%"=="1" (
    echo.
    echo [临时配置]
    setx ZHIPUAI_API_KEY "%API_KEY%" > nul
    set ZHIPUAI_API_KEY=%API_KEY%
    echo   [√] API Key 已临时设置
    echo   [提示] 重启命令行窗口后生效
    echo.
    echo 当前配置的 API Key: %API_KEY%
    echo.
    echo 现在可以运行 启动系统.bat 启动服务
) else if "%CHOICE%"=="2" (
    echo.
    echo [永久配置]
    setx ZHIPUAI_API_KEY "%API_KEY%"
    if errorlevel 1 (
        echo   [×] 设置失败，请以管理员身份运行
    ) else (
        echo   [√] API Key 已永久保存到系统环境变量
        echo   [提示] 重启命令行窗口后生效
        echo.
        echo 当前配置的 API Key: %API_KEY%
    )
    echo.
    echo 现在可以运行 启动系统.bat 启动服务
) else (
    echo.
    echo [错误] 无效的选择
    pause
    exit /b 1
)

echo.
echo ========================================
echo    测试配置
echo ========================================
echo.
echo 重启命令行窗口后，运行以下命令测试：
echo   echo %%ZHIPUAI_API_KEY%%
echo.
echo ========================================
pause

