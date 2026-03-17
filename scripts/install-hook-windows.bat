@echo off
chcp 936 >nul
echo ====================================
echo   Git Pre-commit Hook Setup
echo ====================================
echo.

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%"
set "HOOK_DIR=%PROJECT_DIR%.git\hooks"

if not exist "%PROJECT_DIR%.git" (
    echo Error: Not a Git repository
    pause
    exit /b 1
)

if not exist "%HOOK_DIR%" (
    mkdir "%HOOK_DIR%"
)

set "SOURCE_PS1=%PROJECT_DIR%scripts\pre-commit.ps1"
set "TARGET_PS1=%HOOK_DIR%pre-commit.ps1"
set "TARGET_BAT=%HOOK_DIR%pre-commit.bat"

echo Copying PowerShell script...
copy /Y "%SOURCE_PS1%" "%TARGET_PS1%"

echo Creating pre-commit.bat...
(
echo @echo off
echo powershell -ExecutionPolicy Bypass -File "%%~dp0pre-commit.ps1" %%*
) > "%TARGET_BAT%"

echo.
echo ====================================
echo   Installation Complete!
echo ====================================
echo.
echo Hooks location: %HOOK_DIR%
echo.
pause
