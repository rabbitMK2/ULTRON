@echo off
chcp 65001 >nul
echo 清理 MNN 模块构建缓存...

cd /d "%~dp0"

REM 清理 MNN 模块的构建输出
if exist "mnn\build" (
    echo 删除 mnn\build 目录...
    rmdir /s /q "mnn\build"
)

REM 清理 CMake 构建缓存（包含旧路径引用）
if exist "mnn\.cxx" (
    echo 删除 mnn\.cxx 目录（CMake 缓存）...
    rmdir /s /q "mnn\.cxx"
)

REM 清理根目录的 .cxx（如果有）
if exist ".cxx" (
    echo 删除根目录 .cxx...
    rmdir /s /q ".cxx"
)

REM 清理 Gradle 构建缓存
echo 清理 Gradle 构建缓存...
call gradlew.bat clean --no-daemon 2>nul

echo.
echo 清理完成！现在可以重新构建项目。
echo 请运行: gradlew.bat build
pause

