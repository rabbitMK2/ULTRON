# PowerShell script to convert AIDL files to UTF-8 encoding
$aidlDir = "D:\BaiduNetdiskDownload\奥创最终版\Operit-main\terminal\src\main\aidl"
$files = Get-ChildItem -Path $aidlDir -Filter "*.aidl" -Recurse

foreach ($file in $files) {
    Write-Host "Converting $($file.FullName) to UTF-8..."
    try {
        # Read content with automatic encoding detection
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8 -ErrorAction Stop
        # Write back with UTF-8 encoding (no BOM)
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.UTF8Encoding]::new($false))
        Write-Host "  ✓ Converted successfully"
    } catch {
        Write-Host "  ✗ Error: $_"
    }
}

Write-Host "`nAll AIDL files have been converted to UTF-8 encoding (without BOM)."

