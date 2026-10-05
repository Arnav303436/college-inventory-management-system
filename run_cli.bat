@echo off

if not exist "bin\com\college\inventory\Main.class" (
    echo [INFO] Binaries not found. Running build.bat first...
    call build.bat
    if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
)

echo Starting CampusAsset Pro in Console / CLI Mode...
java -cp "bin;lib\*" com.college.inventory.Main --cli %*
