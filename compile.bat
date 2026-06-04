@echo off
setlocal
cd /d "%~dp0"
if not exist out mkdir out
javac -d out -sourcepath src ^
  src\delivery\Main.java ^
  src\delivery\util\ConsoleUI.java ^
  src\delivery\menu\CustomerMenu.java ^
  src\delivery\menu\OrderMenu.java ^
  src\delivery\menu\AnalysisMenu.java ^
  src\delivery\menu\AdminMenu.java ^
  src\delivery\service\CustomerService.java ^
  src\delivery\service\OrderService.java ^
  src\delivery\service\AnalysisService.java ^
  src\delivery\service\AdminService.java
if errorlevel 1 exit /b 1
echo Build OK. Run: run.bat
exit /b 0
