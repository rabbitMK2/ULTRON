@echo off
REM 简单的清理脚本 - 仅清理 AIDL 相关构建文件
REM 不依赖 Java，可以在 Android Studio 中构建

chcp 65001 >nul 2>&1
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

echo ========================================
echo   清理 AIDL 构建缓存（无需 Java）
echo ========================================
echo.

echo 正在清理构建缓存...

REM 清理 terminal 模块构建目录
if exist "%SCRIPT_DIR%terminal\build" (
    rd /s /q "%SCRIPT_DIR%terminal\build" 2>nul
    if %ERRORLEVEL% EQU 0 (
        echo [OK] Terminal 构建目录已清理
    ) else (
        echo [WARN] 清理 Terminal 构建目录时出错（可能被占用）
    )
) else (
    echo [INFO] Terminal 构建目录不存在
)

REM 清理 .gradle 缓存（可选）
if exist "%SCRIPT_DIR%.gradle" (
    rd /s /q "%SCRIPT_DIR%.gradle\buildOutputCleanup" 2>nul
    echo [INFO] Gradle 缓存已清理
)

echo.
echo ========================================
echo   [OK] 清理完成！
echo ========================================
echo.
echo 下一步:
echo 1. 在 Android Studio 中: Build -^> Clean Project
echo 2. 然后: Build -^> Rebuild Project
echo.
echo 或者在 Android Studio 的 Terminal 中运行:
echo   gradlew clean :terminal:compileDebugAidl
echo.
pause

