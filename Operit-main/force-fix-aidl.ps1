# Simple PowerShell script to force-convert all AIDL files to UTF-8 (no BOM)
# Keep output ASCII-only to avoid parsing issues.

$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$aidlDir = Join-Path $baseDir "terminal\src\main\aidl"

Write-Host "========================================"
Write-Host "  Fix AIDL encoding to UTF-8"
Write-Host "========================================"
Write-Host ""

if (-not (Test-Path $aidlDir)) {
    Write-Host "ERROR: AIDL dir not found: $aidlDir"
    exit 1
}

$files = Get-ChildItem -Path $aidlDir -Filter "*.aidl" -Recurse

if ($files.Count -eq 0) {
    Write-Host "No AIDL files found in $aidlDir"
    exit 1
}

Write-Host "Found $($files.Count) AIDL files"
Write-Host ""

$successCount = 0

foreach ($file in $files) {
    try {
        Write-Host "Processing: $($file.Name)"

        # Read bytes, strip BOM if present
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $bytes = $bytes[3..($bytes.Length-1)]
        }

        # Try UTF-8 decode, fallback to ANSI
        try {
            $content = [System.Text.Encoding]::UTF8.GetString($bytes)
        } catch {
            $content = [System.Text.Encoding]::Default.GetString($bytes)
        }

        # Normalize newlines to LF
        $content = $content -replace "`r`n", "`n" -replace "`r", "`n"

        # Ensure trailing newline
        if (-not $content.EndsWith("`n")) {
            $content += "`n"
        }

        # Write as UTF-8 without BOM
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)

        Write-Host "  OK"
        $successCount++
    } catch {
        Write-Host "  ERROR: $($_.Exception.Message)"
    }
}

Write-Host ""
Write-Host "========================================"
Write-Host "Done: $successCount / $($files.Count) files converted"
Write-Host "========================================"
Write-Host ""
Write-Host "Next: gradlew clean :terminal:compileDebugAidl"

