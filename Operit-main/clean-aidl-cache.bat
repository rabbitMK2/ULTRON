@echo off
chcp 65001 >nul
echo 正在清理 AIDL 构建缓存...

if exist "terminal\build\generated\source\aidl" (
    rmdir /s /q "terminal\build\generated\source\aidl"
    echo AIDL 缓存已清理
) else (
    echo AIDL 缓存目录不存在
)

if exist "terminal\build" (
    rmdir /s /q "terminal\build"
    echo Terminal 模块构建目录已清理
)

echo 清理完成！请重新构建项目。
