@echo off
setlocal enabledelayedexpansion
title 停止应用商店系统所有服务
echo ========================================
echo   停止应用商店系统所有服务
echo ========================================
echo.

REM ============================================
REM 安全说明：仅按端口精准停止本项目服务，
REM 不再使用 taskkill /IM java.exe 或 node.exe，
REM 避免误杀电脑上其他 Java/Node 程序（如IDEA、其他开发服务）。
REM ============================================

echo [1/4] 停止后端服务(端口8080)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
    echo   结束后端进程 PID=%%a
    taskkill /F /PID %%a >nul 2>&1
)
echo   后端已停止

echo [2/4] 停止前端服务(端口5173/5174/5175)...
for %%P in (5173 5174 5175) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%P" ^| findstr "LISTENING"') do (
        echo   结束端口%%P进程 PID=%%a
        taskkill /F /PID %%a >nul 2>&1
    )
)
echo   前端服务已停止

echo [3/4] 关闭相关CMD窗口...
for %%T in ("后端服务-8080" "用户端-5173" "作者端-5174" "管理后台-5175") do (
    for /f "tokens=2" %%a in ('tasklist /fi "windowtitle eq %%T" /fo list ^| findstr "PID"') do taskkill /F /PID %%a >nul 2>&1
)
echo   服务窗口已关闭

echo [4/4] 验证端口已释放...
timeout /t 2 /nobreak >nul
for %%P in (8080 5173 5174 5175) do (
    netstat -ano | findstr ":%%P" | findstr "LISTENING" >nul
    if !errorlevel!==0 (
        echo   [警告] 端口%%P仍被占用
    ) else (
        echo   端口%%P已释放
    )
)

echo.
echo ========================================
echo   所有服务已停止！
echo   MySQL和Redis保持运行（如需停止请手动结束对应进程）
echo ========================================
echo.
pause