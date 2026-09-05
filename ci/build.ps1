#Requires -Version 5.1
<#
Run the canonical Linux build inside WSL. Install JDK 25+, Python 3.9+, curl,
unzip and zip in WSL first. See ci/README.md. The Windows JDK alone is not used.
Usage: powershell -ExecutionPolicy Bypass -File ci\build.ps1 [-Version 1.9.101]
#>
param([string]$Version = "")
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
if (-not (Get-Command wsl.exe -ErrorAction SilentlyContinue)) {
    throw "WSL is required. Install WSL and the Linux build tools listed in ci/README.md."
}
if ($Version -and $Version -notmatch '^\d+\.\d+\.\d+$') {
    throw "Invalid version: $Version"
}
if ($Version) {
    & wsl.exe --cd $root bash ci/build.sh $Version
} else {
    & wsl.exe --cd $root bash ci/build.sh
}
if ($LASTEXITCODE -ne 0) {
    throw "MCSM compilation failed (exit $LASTEXITCODE). No new JAR was built."
}
