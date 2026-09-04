#Requires -Version 5.1
<#
 ci/build.ps1 — compile the MCSM extras and assemble the jar ON YOUR PC.

 You already have everything this needs: Windows 11, a JDK 25 (your Azul
 Zulu 25 works — the same one Minecraft uses), and internet. PowerShell ships
 with Windows, so there is nothing to install.

   1. Open PowerShell.
   2. cd into your clone of the repo  (cd C:\...\Lowuuuuuu)
   3. powershell -ExecutionPolicy Bypass -File ci\build.ps1
        (optionally:  -Version 1.9.98  to pin a version)

 Output lands in  .\out\dabywitherstormmod-<version>-26.2-beta-mcsm.jar
 Put that ONE jar in your mods folder, replacing older builds.

 This is the same recipe as ci/build.sh (the one GitHub Actions runs once the
 workflow file is installed — see ci\README.md).
#>

param([string]$Version = "")

$ErrorActionPreference = "Stop"
$root = (Get-Location).Path
if ($Version -eq "") { $Version = (Get-Content "$root\VERSION").Trim() }
$jarId = "$Version-26.2-beta-mcsm"
Write-Host "[build] MCSM $jarId"

# --- newest delivery jar as the base -------------------------------------
$base = Get-ChildItem "$root\delivery\dabywitherstormmod-*-26.2-beta-mcsm.jar" |
        Sort-Object { [version]($_.Name -replace '.*?(\d+\.\d+\.\d+)-.*','$1') } |
        Select-Object -Last 1
Write-Host "[build] base jar: $($base.Name)"

# --- dependencies (only downloaded once, cached in .\ci\.\cache) -----------
$cache = "$root\ci\cache"
New-Item -ItemType Directory -Force -Path $cache | Out-Null
$deps = @(
  @{ url = "https://piston-data.mojang.com/v1/objects/2dc72797acbc1b63fc16a11c4ac393605f453754/client.jar"; file = "client.jar" },
  @{ url = "https://repo1.maven.org/maven2/net/fabricmc/sponge-mixin/0.15.4+mixin.0.8.7/sponge-mixin-0.15.4+mixin.0.8.7.jar"; file = "mixin.jar" },
  @{ url = "https://repo1.maven.org/maven2/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar"; file = "jspecify.jar" },
  @{ url = "https://repo1.maven.org/maven2/it/unimi/dsi/fastutil/8.5.15/fastutil-8.5.15.jar"; file = "fastutil.jar" },
  @{ url = "https://libraries.minecraft.net/com/mojang/datafixerupper/8.0.16/datafixerupper-8.0.16.jar"; file = "dfu.jar" },
  @{ url = "https://libraries.minecraft.net/org/joml/joml/1.10.8/joml-1.10.8.jar"; file = "joml.jar" }
)
foreach ($d in $deps) {
  $out = Join-Path $cache $d.file
  if (-not (Test-Path $out)) {
    Write-Host "[deps] downloading $($d.file) ..."
    Invoke-WebRequest -Uri $d.url -OutFile $out
  }
}

# --- find javac -------------------------------------------------------------
$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
if (-not $javac) { $javac = "$env:JAVA_HOME\bin\javac.exe" }
if (-not (Test-Path $javac)) { throw "javac not found — install/point JAVA_HOME at your JDK 25 (the Azul one you play with works)." }
$javaBin = Split-Path $javac           # all JDK tools live beside javac
$jarTool = Join-Path $javaBin "jar.exe"
& $javac -version

# --- compile ----------------------------------------------------------------
$buildDir = "$root\ci\out-classes"
if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null
$sources = Get-ChildItem -Recurse "$root\mcsm-extras\java" -Filter *.java | ForEach-Object { $_.FullName }
$cp = @( "$cache\client.jar"; $base.FullName; "$cache\mixin.jar"; "$cache\jspecify.jar"; "$cache\fastutil.jar"; "$cache\dfu.jar"; "$cache\joml.jar" ) -join ";"
& $javac -nowarn --release 25 -proc:none -cp "$cp" -d "$buildDir" $sources
if ($LASTEXITCODE -ne 0) { throw "javac failed with exit code $LASTEXITCODE" }
Write-Host "[javac] OK: $((Get-ChildItem -Recurse $buildDir -Filter *.class).Count) classes"

# --- assemble the new jar ---------------------------------------------------
$fx = "$root\ci\out-unzip"
if (Test-Path $fx) { Remove-Item -Recurse -Force $fx }
New-Item -ItemType Directory -Force -Path $fx | Out-Null
Write-Host "[assemble] extracting base..."
Push-Location $fx
try { & $jarTool -xf $base.FullName } finally { Pop-Location }

Copy-Item -Recurse -Force "$root\mcsm-core-shaders\*" "$fx\assets\minecraft\shaders\"
Copy-Item -Recurse -Force "$root\jar-overrides\*" "$fx\"
Copy-Item -Recurse -Force "$buildDir\*" "$fx\"

$fmj = "$fx\fabric.mod.json"
(Get-Content $fmj -Raw) -replace '"version": "[0-9.]+-26\.2-beta-mcsm"', """version"": ""$jarId""" | Set-Content $fmj -Encoding ASCII

$outDir = "$root\out"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$outJar = "$outDir\dabywitherstormmod-$jarId.jar"
if (Test-Path $outJar) { Remove-Item -Force $outJar }
Push-Location $fx
try {
  & $jarTool -cf $outJar .
  if ($LASTEXITCODE -ne 0) { throw "jar packaging failed" }
} finally { Pop-Location }

$hash = (Get-FileHash $outJar -Algorithm SHA256).Hash.ToLower()
"$hash  $(Split-Path $outJar -Leaf)" | Set-Content "$outJar.sha256" -Encoding ASCII
Write-Host "[done] $outJar"
Write-Host "[sha256] $hash"
Write-Host "Put this ONE jar in your mods folder. Remove older dabywitherstormmod jars first."
