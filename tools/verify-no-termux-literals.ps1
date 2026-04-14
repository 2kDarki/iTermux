$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$legacyPatterns = @(
    "com\.termux",
    "/data/data/com\.termux"
)

$scanTargets = @(
    (Join-Path $repoRoot "core"),
    (Join-Path $repoRoot "proot-plugin"),
    (Join-Path $repoRoot "sample-app"),
    (Join-Path $repoRoot "build.gradle.kts"),
    (Join-Path $repoRoot "settings.gradle.kts"),
    (Join-Path $repoRoot "gradle.properties")
)

$sourceFiles = foreach ($target in $scanTargets) {
    if (Test-Path $target -PathType Container) {
        Get-ChildItem -Path $target -Recurse -File |
            Where-Object {
                $_.FullName -notmatch "\\build\\" -and
                $_.FullName -notmatch "\\src\\test\\" -and
                $_.FullName -notmatch "\\src\\androidTest\\"
            }
    } elseif (Test-Path $target -PathType Leaf) {
        Get-Item $target
    }
}

$legacyHits = @()
foreach ($pattern in $legacyPatterns) {
    $legacyHits += $sourceFiles |
        Select-String -Pattern $pattern |
        ForEach-Object {
            "{0}:{1}: {2}" -f $_.Path, $_.LineNumber, $_.Line.Trim()
        }
}

if ($legacyHits.Count -gt 0) {
    Write-Host "Legacy Termux literals found in active project files:" -ForegroundColor Red
    $legacyHits | Sort-Object -Unique | ForEach-Object { Write-Host "  $_" }
    exit 1
}

$marker = "INTERNAL-TERMUX MODIFIED - merge carefully"
$escapedMarker = [regex]::Escape($marker)
$coreFiles = Get-ChildItem -Path (Join-Path $repoRoot "core\\src\\main\\java\\com\\darkian\\itermux\\core") -Filter *.kt -File
$missingMarkers = foreach ($file in $coreFiles) {
    if (-not (Select-String -Path $file.FullName -Pattern $escapedMarker -Quiet)) {
        $file.FullName
    }
}

if ($missingMarkers.Count -gt 0) {
    Write-Host "Missing merge markers in core runtime files:" -ForegroundColor Red
    $missingMarkers | ForEach-Object { Write-Host "  $_" }
    exit 1
}

Write-Host "Legacy literal scan passed."
Write-Host "Merge-marker scan passed."
