# 修复 AIDL 文件编码为 UTF-8 (无 BOM)
$aidlPath = "D:\BaiduNetdiskDownload\奥创最终版\Operit-main\terminal\src\main\aidl"

Write-Host "正在修复 AIDL 文件编码..."

Get-ChildItem -Path $aidlPath -Filter "*.aidl" -Recurse | ForEach-Object {
    $filePath = $_.FullName
    Write-Host "处理: $filePath"
    
    # 读取文件内容（自动检测编码）
    $content = Get-Content -Path $filePath -Raw -Encoding UTF8
    
    # 以 UTF-8 (无 BOM) 重新保存
    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllText($filePath, $content, $utf8NoBom)
    
    Write-Host "已转换为 UTF-8 (无 BOM): $filePath"
}

Write-Host "完成！所有 AIDL 文件已转换为 UTF-8 (无 BOM) 编码。"

