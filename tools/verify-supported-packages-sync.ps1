$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$rootFile = Join-Path $repoRoot "supported-packages.txt"
$assetFile = Join-Path $repoRoot "core\\src\\main\\assets\\itermux\\supported-packages.txt"
$bootstrapAsset = Join-Path $repoRoot "core\\src\\main\\assets\\itermux\\bootstrap\\bootstrap.tar.xz"

if (-not (Test-Path $rootFile -PathType Leaf)) {
    Write-Host "Missing root supported-packages.txt" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $assetFile -PathType Leaf)) {
    Write-Host "Missing asset supported-packages.txt" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $bootstrapAsset -PathType Leaf)) {
    Write-Host "Missing packaged bootstrap asset." -ForegroundColor Red
    exit 1
}

$bootstrapInfo = Get-Item $bootstrapAsset
if ($bootstrapInfo.Length -le 0) {
    Write-Host "Packaged bootstrap asset is empty." -ForegroundColor Red
    exit 1
}

$rootContent = Get-Content -Raw $rootFile
$assetContent = Get-Content -Raw $assetFile

if ($rootContent -ne $assetContent) {
    Write-Host "supported-packages.txt is out of sync with the packaged asset." -ForegroundColor Red
    exit 1
}

Write-Host "Supported package files are in sync."
Write-Host "Packaged bootstrap asset is present."
