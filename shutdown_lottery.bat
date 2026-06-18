@echo off

echo Shutting down the Lottery application...

set "PROJECT_DIR=%cd%"
set "TOMCAT_DIR=%PROJECT_DIR%\deploy\apache-tomcat-10.1.28"

echo Project directory: %PROJECT_DIR%
echo Tomcat directory: %TOMCAT_DIR%

echo Initializing Tomcat environment...
call "%TOMCAT_DIR%\bin\setenv.bat"

if not exist "%TOMCAT_DIR%" (
    echo Error: Tomcat directory not found at %TOMCAT_DIR%
    exit /b 1
)

echo Stopping Tomcat server...
call "%TOMCAT_DIR%\bin\shutdown.bat"

if %errorlevel% neq 0 (
    echo Warning: Tomcat shutdown script returned an error, but continuing with process termination...
)

echo Waiting for graceful shutdown...
timeout /t 5 /nobreak > nul

echo Killing any remaining Java processes...
taskkill /F /IM java.exe 2>nul

if %errorlevel% equ 0 (
    echo Java processes terminated.
) else (
    echo No Java processes found or unable to terminate.
)

echo.
echo [SUCCESS] Lottery application shutdown completed!
echo Tomcat server has been stopped and Java processes terminated.

exit /b 0
