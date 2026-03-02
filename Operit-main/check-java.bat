@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   检查 Java 环境配置
echo ========================================
echo.

echo 1. 检查 JAVA_HOME 环境变量:
if defined JAVA_HOME (
    echo    [OK] JAVA_HOME = %JAVA_HOME%
    if exist "%JAVA_HOME%\bin\java.exe" (
        echo    [OK] Java 可执行文件存在
        echo.
        echo    Java 版本信息:
        "%JAVA_HOME%\bin\java.exe" -version 2>&1
    ) else (
        echo    [ERROR] Java 可执行文件不存在
    )
) else (
    echo    [WARN] JAVA_HOME 未设置
)
echo.

echo 2. 检查 PATH 中的 java 命令:
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo    [OK] 在 PATH 中找到 java 命令
    for /f "tokens=*" %%i in ('where java') do echo    路径: %%i
    echo.
    echo    Java 版本信息:
    java -version 2>&1
) else (
    echo    [WARN] 在 PATH 中未找到 java 命令
)
echo.

echo 3. 常见的 Java 安装位置:
set "JAVA_PATHS[0]=C:\Program Files\Java"
set "JAVA_PATHS[1]=C:\Program Files (x86)\Java"
set "JAVA_PATHS[2]=C:\Program Files\Android\Android Studio\jbr"
set "JAVA_PATHS[3]=C:\Program Files\Android\Android Studio\jre"

for /L %%i in (0,1,3) do (
    call set "CURRENT_PATH=%%JAVA_PATHS[%%i]%%"
    if exist "!CURRENT_PATH!" (
        echo    [FOUND] !CURRENT_PATH!
        for /d %%d in ("!CURRENT_PATH!\*") do (
            if exist "%%d\bin\java.exe" (
                echo       - %%d
            )
        )
    )
)
echo.

echo ========================================
echo   设置 JAVA_HOME 的示例命令:
echo ========================================
echo setx JAVA_HOME "C:\Program Files\Java\jdk-17" /M
echo 注意: 需要管理员权限，或去掉 /M 只设置当前用户
echo.
echo 或者临时设置（仅本次会话有效）:
echo set JAVA_HOME=C:\Program Files\Java\jdk-17
echo.
pause

