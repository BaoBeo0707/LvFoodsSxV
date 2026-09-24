@echo off
setlocal
set "GRADLE_VERSION=9.5.0"
set "GRADLE_HOME=%~dp0.gradle-dist\gradle-%GRADLE_VERSION%"
set "GRADLE_ZIP=%TEMP%\gradle-%GRADLE_VERSION%-bin.zip"

if exist "%GRADLE_HOME%\bin\gradle.bat" goto run

echo [Lv Foods SxV] Gradle %GRADLE_VERSION% not found. Downloading it...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_ZIP%'"
if errorlevel 1 (
  echo Failed to download Gradle. Check your internet connection.
  exit /b 1
)

if not exist "%~dp0.gradle-dist" mkdir "%~dp0.gradle-dist"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force -Path '%GRADLE_ZIP%' -DestinationPath '%~dp0.gradle-dist'"
if errorlevel 1 (
  echo Failed to extract Gradle.
  exit /b 1
)

del /q "%GRADLE_ZIP%" >nul 2>&1

:run
call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
