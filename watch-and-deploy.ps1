# MedLink Yemen - Build & Deploy Watch Script
# يراقب تغييرات الملفات ويعيد البناء والتثبيت تلقائياً

$env:JAVA_HOME = "C:\Program Files\Android\Android Studio1\jbr"
$ADB = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$PROJECT_DIR = "c:\Users\USER\MedLin-1"
$APK_PATH = "$PROJECT_DIR\app\build\outputs\apk\debug\app-debug.apk"
$PACKAGE = "com.example"
$ACTIVITY = ".MainActivity"

$watchDirs = @(
    "$PROJECT_DIR\app\src\main\java",
    "$PROJECT_DIR\app\src\main\res"
)

function Get-LatestWriteTime {
    $latest = [datetime]::MinValue
    foreach ($dir in $watchDirs) {
        if (Test-Path $dir) {
            $files = Get-ChildItem -Path $dir -Recurse -File -ErrorAction SilentlyContinue
            foreach ($f in $files) {
                if ($f.LastWriteTime -gt $latest) {
                    $latest = $f.LastWriteTime
                }
            }
        }
    }
    return $latest
}

function Build-And-Deploy {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Building MedLink Yemen..." -ForegroundColor Cyan
    Write-Host "  $(Get-Date -Format 'HH:mm:ss')" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    
    $startTime = Get-Date
    Push-Location $PROJECT_DIR
    
    # Build
    & .\gradlew.bat assembleDebug --no-daemon 2>&1 | Where-Object {
        $_ -match "(BUILD|FAILED|error:|SUCCESSFUL|Task|EXECUTING)"
    } | ForEach-Object {
        if ($_ -match "error:") { Write-Host $_ -ForegroundColor Red }
        elseif ($_ -match "FAILED") { Write-Host $_ -ForegroundColor Red }
        elseif ($_ -match "SUCCESSFUL") { Write-Host $_ -ForegroundColor Green }
        else { Write-Host $_ -ForegroundColor Gray }
    }
    
    $buildTime = (Get-Date) - $startTime
    Pop-Location
    
    if (Test-Path $APK_PATH) {
        # Check if build was successful by comparing APK time
        $apkTime = (Get-Item $APK_PATH).LastWriteTime
        if ($apkTime -gt $startTime) {
            Write-Host ""
            Write-Host "  Build OK! ($([math]::Round($buildTime.TotalSeconds))s)" -ForegroundColor Green
            Write-Host "  Installing on device..." -ForegroundColor Cyan
            
            # Install
            & $ADB install -r $APK_PATH 2>&1 | ForEach-Object {
                if ($_ -match "Success") { Write-Host "  $_" -ForegroundColor Green }
                else { Write-Host "  $_" -ForegroundColor Yellow }
            }
            
            # Launch
            & $ADB shell am start -n "$PACKAGE/$ACTIVITY" 2>&1 | Out-Null
            Write-Host "  App launched!" -ForegroundColor Green
            Write-Host ""
        } else {
            Write-Host "  Build failed! Check errors above." -ForegroundColor Red
        }
    } else {
        Write-Host "  Build failed! APK not found." -ForegroundColor Red
    }
    
    Write-Host "----------------------------------------" -ForegroundColor DarkGray
    Write-Host "  Watching for changes... (Ctrl+C to stop)" -ForegroundColor DarkYellow
    Write-Host "----------------------------------------" -ForegroundColor DarkGray
}

# === Main ===
Write-Host ""
Write-Host "============================================" -ForegroundColor Magenta
Write-Host "  MedLink Yemen - Auto Build & Deploy" -ForegroundColor Magenta
Write-Host "  Watching: java/, res/" -ForegroundColor White
Write-Host "  Press Ctrl+C to stop" -ForegroundColor DarkGray
Write-Host "============================================" -ForegroundColor Magenta

# Initial build
Build-And-Deploy

# Watch loop
$lastTime = Get-LatestWriteTime
while ($true) {
    Start-Sleep -Seconds 2
    $currentTime = Get-LatestWriteTime
    if ($currentTime -gt $lastTime) {
        Write-Host "  Change detected!" -ForegroundColor Yellow
        $lastTime = $currentTime
        Start-Sleep -Seconds 1  # Wait for file write to finish
        Build-And-Deploy
        $lastTime = Get-LatestWriteTime
    }
}
