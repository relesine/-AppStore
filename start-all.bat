@echo off
setlocal enabledelayedexpansion
title 轻量应用商店系统 - 一键启动
echo ========================================
echo   轻量应用商店系统 一键启动脚本
echo ========================================
echo.

REM ============================================
REM 第1步：检查并启动MySQL
REM ============================================
echo [1/6] 检查MySQL服务...
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
if !errorlevel!==0 (
    echo   MySQL已在运行 (端口3306)
) else (
    echo   MySQL未运行，正在启动...
    REM 自动探测本机MySQL安装位置（按优先级尝试，可在下方自行修改路径）
    set "MYSQL_FOUND="
    for %%M in (
        "D:\phpstudy_pro\Extensions\MySQL8.0.12"
        "F:\phpstudy_pro\Extensions\MySQL8.0.12"
        "C:\Program Files\MySQL\MySQL Server 8.0"
        "D:\MySQL\MySQL Server 8.0"
        "F:\MySQL\MySQL Server 8.0"
    ) do (
        if not defined MYSQL_FOUND (
            if exist "%%~M\bin\mysqld.exe" set "MYSQL_FOUND=%%~M"
        )
    )
    if defined MYSQL_FOUND (
        echo   找到MySQL安装目录: !MYSQL_FOUND!
        if exist "!MYSQL_FOUND!\my.ini" (
            start "" /min "!MYSQL_FOUND!\bin\mysqld.exe" --defaults-file="!MYSQL_FOUND!\my.ini"
        ) else (
            start "" /min "!MYSQL_FOUND!\bin\mysqld.exe"
        )
        timeout /t 6 /nobreak >nul
        netstat -ano | findstr ":3306" | findstr "LISTENING" >nul
        if !errorlevel!==0 (
            echo   MySQL启动成功
        ) else (
            echo   [警告] MySQL启动失败，请手动启动MySQL后重试
        )
    ) else (
        echo   [警告] 未找到MySQL安装目录，请手动启动MySQL
    )
)
echo.

REM ============================================
REM 第2步：检查并启动Redis
REM ============================================
echo [2/6] 检查Redis服务...
redis-cli ping >nul 2>&1
if !errorlevel!==0 (
    echo   Redis已在运行 (端口6379)
) else (
    echo   Redis未运行，正在启动...
    if exist "F:\Redis\redis-server.exe" (
        start "Redis-6379" /min "F:\Redis\redis-server.exe" "F:\Redis\redis.windows.conf"
        timeout /t 3 /nobreak >nul
        redis-cli ping >nul 2>&1
        if !errorlevel!==0 (
            echo   Redis启动成功
        ) else (
            echo   [警告] Redis启动失败，请手动启动 F:\Redis\redis-server.exe
        )
    ) else (
        echo   [警告] 未找到 F:\Redis\redis-server.exe，请安装Redis或手动启动
    )
)
echo.

REM ============================================
REM 第3步：清理端口冲突（仅清理本项目端口，避免误杀其他服务）
REM ============================================
echo [3/6] 检查端口冲突...
for %%P in (8080 5173 5174 5175) do (
    netstat -ano | findstr ":%%P" | findstr "LISTENING" >nul
    if !errorlevel!==0 (
        for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%P" ^| findstr "LISTENING"') do (
            echo   端口%%P被占用(PID=%%a)，正在释放...
            taskkill /F /PID %%a >nul 2>&1
        )
    )
)
echo   端口检查完成
echo.

REM ============================================
REM 第4步：启动后端
REM ============================================
echo [4/6] 启动后端服务 (端口8080)...
start "后端服务-8080" cmd /k "cd /d F:\AI_full-stack_project\AppStore\backend && mvn spring-boot:run"
echo   后端启动中，请等待约20秒...
timeout /t 20 /nobreak >nul
echo.

REM ============================================
REM 第5步：启动用户端
REM ============================================
echo [5/6] 启动用户端 (端口5173)...
start "用户端-5173" cmd /k "cd /d F:\AI_full-stack_project\AppStore\frontend-user && npm run dev"
timeout /t 3 /nobreak >nul
echo.

REM ============================================
REM 第6步：启动作者端和管理后台
REM ============================================
echo [6/6] 启动作者端(5174)和管理后台(5175)...
start "作者端-5174" cmd /k "cd /d F:\AI_full-stack_project\AppStore\frontend-author && npm run dev"
start "管理后台-5175" cmd /k "cd /d F:\AI_full-stack_project\AppStore\frontend-admin && npm run dev"
echo.

echo ========================================
echo   全部服务启动中！
echo   请等待各窗口显示"就绪"或"Local:"
echo.
echo   访问地址：
echo   用户端:   http://localhost:5173
echo   作者端:   http://localhost:5174
echo   管理后台: http://localhost:5175
echo   后端API:  http://localhost:8080/api
echo ========================================
echo.
echo 测试账号（密码均为123456）：
echo   普通用户: user1
echo   作者:     author1
echo   管理员:   admin
echo.
echo 提示：关闭各CMD窗口即可停止对应服务；或运行 stop-all.bat 精准停止
pause