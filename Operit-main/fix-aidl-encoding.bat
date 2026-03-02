@echo off
chcp 65001 >nul
echo 正在修复 AIDL 文件编码为 UTF-8...
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0fix-aidl-encoding.ps1"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo 修复完成！请运行: gradlew clean build
) else (
    echo.
    echo 修复过程中出现错误，请检查 PowerShell 脚本
)

pause
