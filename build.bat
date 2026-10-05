@echo off
setlocal enabledelayedexpansion

echo ===================================================
echo   Compiling CampusAsset Pro (Java Mini Project)
echo ===================================================

if not exist "bin" mkdir "bin"

rem Gather all Java source files into sources.txt with proper quotes
powershell -NoProfile -Command "Get-ChildItem -Recurse -Filter '*.java' src | ForEach-Object { '\"' + $_.FullName.Replace('\', '/') + '\"' } | Out-File -Encoding ascii sources.txt"

echo Compiling Java source files...
javac -encoding UTF-8 -cp "lib\*" -d "bin" @sources.txt

if %ERRORLEVEL% equ 0 (
    echo.
    echo ===================================================
    echo   BUILD SUCCESSFUL!
    echo   Output directory: bin/
    echo ===================================================
    if exist sources.txt del sources.txt
) else (
    echo.
    echo [ERROR] Compilation failed with error code %ERRORLEVEL%.
    if exist sources.txt del sources.txt
    exit /b %ERRORLEVEL%
)
