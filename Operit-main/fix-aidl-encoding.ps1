# PowerShell script to fix AIDL file encoding to UTF-8
# 将 AIDL 文件转换为 UTF-8 编码

$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$aidlDir = Join-Path $PSScriptRoot "terminal\src\main\aidl"
$files = Get-ChildItem -Path $aidlDir -Filter "*.aidl" -Recurse

Write-Host "找到 $($files.Count) 个 AIDL 文件" -ForegroundColor Green

foreach ($file in $files) {
    try {
        # 读取文件内容（尝试不同的编码）
        $content = $null
        $encodings = @([System.Text.Encoding]::UTF8, [System.Text.Encoding]::Default, [System.Text.Encoding]::ASCII)
        
        foreach ($encoding in $encodings) {
            try {
                $content = [System.IO.File]::ReadAllText($file.FullName, $encoding)
                Write-Host "文件 $($file.Name) 使用编码: $($encoding.EncodingName)" -ForegroundColor Yellow
                break
            } catch {
                continue
            }
        }
        
        if ($content -eq $null) {
            Write-Host "无法读取文件: $($file.FullName)" -ForegroundColor Red
            continue
        }
        
        # 使用 UTF-8 无 BOM 编码保存
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
        Write-Host "已转换文件: $($file.Name) -> UTF-8" -ForegroundColor Green
    } catch {
        Write-Host "处理文件 $($file.Name) 时出错: $_" -ForegroundColor Red
    }
}

Write-Host "`n所有 AIDL 文件已转换为 UTF-8 编码" -ForegroundColor Green
Write-Host "请运行: gradlew clean build" -ForegroundColor Yellow
