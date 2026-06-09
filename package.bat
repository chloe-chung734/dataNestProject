@echo off
setlocal
cd /d "%~dp0"

call compile.bat
if errorlevel 1 exit /b 1

if not exist lib\mysql-connector-j.jar (
  echo ERROR: lib\mysql-connector-j.jar not found.
  exit /b 1
)

(
  echo Main-Class: delivery.Main
  echo Class-Path: lib/mysql-connector-j.jar
  echo.
) > manifest.txt

jar cfm dataNestProject.jar manifest.txt -C out .
del manifest.txt

if errorlevel 1 (
  echo JAR creation failed.
  exit /b 1
)

echo.
echo Created: dataNestProject.jar
echo Run: java -jar dataNestProject.jar
echo   or: run.bat
exit /b 0
