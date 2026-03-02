@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   清理构建缓存并重新构建
echo ========================================
echo.

REM 获取脚本所在目录
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

REM 清理 terminal 模块的构建输出
echo 正在清理 terminal 模块构建缓存...

REM 使用完整路径避免中文路径问题
if exist "%SCRIPT_DIR%terminal\build" (
    rd /s /q "%SCRIPT_DIR%terminal\build" 2>nul
    echo [OK] Terminal 构建目录已清理
) else (
    echo [INFO] Terminal 构建目录不存在
)

if exist "%SCRIPT_DIR%terminal\build\generated\source\aidl" (
    rd /s /q "%SCRIPT_DIR%terminal\build\generated\source\aidl" 2>nul
    echo [OK] AIDL 生成文件已清理
)

echo.
echo ========================================
echo   检查 Java 环境...
echo ========================================
echo.

REM 检查 JAVA_HOME
if defined JAVA_HOME (
    echo [OK] JAVA_HOME 已设置: %JAVA_HOME%
    if exist "%JAVA_HOME%\bin\java.exe" (
        echo [OK] Java 可执行文件存在
        "%JAVA_HOME%\bin\java.exe" -version 2>&1 | findstr /i "version"
    ) else (
        echo [WARN] JAVA_HOME 设置但 Java 可执行文件不存在
    )
) else (
    echo [WARN] JAVA_HOME 未设置，尝试查找 Java...
    where java >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo [OK] 在 PATH 中找到 java 命令
        java -version 2>&1 | findstr /i "version"
    ) else (
        echo [ERROR] 未找到 Java，请设置 JAVA_HOME
        echo.
        echo 解决方法:
        echo 1. 查找 Java 安装路径（通常在 C:\Program Files\Java\ 或 C:\Program Files (x86)\Java\）
        echo 2. 设置环境变量 JAVA_HOME 指向 JDK 目录
        echo 3. 或者在 Android Studio 中构建（会自动使用 Android Studio 的 JDK）
        echo.
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo   正在重新构建项目...
echo ========================================
echo.

REM 检查 gradlew.bat 是否存在
if exist "%SCRIPT_DIR%gradlew.bat" (
    call "%SCRIPT_DIR%gradlew.bat" clean :terminal:compileDebugAidl
    set BUILD_RESULT=%ERRORLEVEL%
) else (
    echo [ERROR] 未找到 gradlew.bat
    echo 请确保在项目根目录运行此脚本
    pause
    exit /b 1
)

echo.
echo ========================================
if %BUILD_RESULT% EQU 0 (
    echo   [OK] 构建成功！
) else (
    echo   [ERROR] 构建失败，错误代码: %BUILD_RESULT%
    echo   请检查上面的错误信息
    echo.
    echo   提示: 如果 JAVA_HOME 未设置，可以在 Android Studio 中构建
)
echo ========================================
echo.
pause

