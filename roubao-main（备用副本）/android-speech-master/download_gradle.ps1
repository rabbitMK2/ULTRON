# Gradle手动下载辅助脚本
# 使用方法：右键以管理员身份运行PowerShell，然后执行此脚本

Write-Host "=== Gradle 8.0 手动下载辅助工具 ===" -ForegroundColor Green
Write-Host ""

# 获取用户名
$username = $env:USERNAME
$gradleBasePath = "C:\Users\$username\.gradle\wrapper\dists\gradle-8.0-bin"

Write-Host "检查Gradle缓存目录..." -ForegroundColor Yellow
if (Test-Path $gradleBasePath) {
    $subDirs = Get-ChildItem -Path $gradleBasePath -Directory
    if ($subDirs.Count -gt 0) {
        $randomString = $subDirs[0].Name
        $targetPath = Join-Path $gradleBasePath $randomString
        Write-Host "找到缓存目录: $targetPath" -ForegroundColor Green
        Write-Host ""
        Write-Host "请按以下步骤操作：" -ForegroundColor Cyan
        Write-Host "1. 使用浏览器下载 Gradle 8.0：" -ForegroundColor White
        Write-Host "   https://mirrors.huaweicloud.com/gradle/gradle-8.0-bin.zip" -ForegroundColor Yellow
        Write-Host "   或" -ForegroundColor White
        Write-Host "   https://mirrors.tuna.tsinghua.edu.cn/gradle/gradle-8.0-bin.zip" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "2. 下载完成后，将 gradle-8.0-bin.zip 文件复制到：" -ForegroundColor White
        Write-Host "   $targetPath" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "3. 然后在Android Studio中：File -> Invalidate Caches / Restart" -ForegroundColor White
        Write-Host ""
        
        # 询问是否打开目录
        $open = Read-Host "是否打开目标目录？(Y/N)"
        if ($open -eq "Y" -or $open -eq "y") {
            explorer.exe $targetPath
        }
    } else {
        Write-Host "未找到随机字符串文件夹，请先在Android Studio中尝试同步一次" -ForegroundColor Red
    }
} else {
    Write-Host "Gradle缓存目录不存在，请先在Android Studio中尝试同步一次" -ForegroundColor Red
    Write-Host "这将创建目录: $gradleBasePath" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

