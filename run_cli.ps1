if (-not (Test-Path "bin\com\college\inventory\Main.class")) {
    Write-Host "[INFO] Binaries not found. Building project first..." -ForegroundColor Cyan
    cmd /c build.bat
}

Write-Host "Starting CampusAsset Pro in Console / CLI Mode..." -ForegroundColor Green
java -cp "bin;lib\*" com.college.inventory.Main --cli $args
