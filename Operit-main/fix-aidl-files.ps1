# PowerShell 脚本：将 AIDL 文件转换为 UTF-8 编码
$ErrorActionPreference = "Stop"

$aidlDir = "D:\BaiduNetdiskDownload\奥创最终版\Operit-main\terminal\src\main\aidl\com\ai\assistance\operit\terminal"
$files = @(
    "$aidlDir\CommandExecutionEvent.aidl",
    "$aidlDir\ITerminalCallback.aidl",
    "$aidlDir\ITerminalService.aidl",
    "$aidlDir\SessionDirectoryEvent.aidl"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Converting $file to UTF-8..."
        $content = Get-Content $file -Raw -Encoding Default
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllLines($file, $content, $utf8NoBom)
        Write-Host "Converted: $file"
    } else {
        Write-Warning "File not found: $file"
    }
}

Write-Host "All AIDL files have been converted to UTF-8 encoding (without BOM)."

