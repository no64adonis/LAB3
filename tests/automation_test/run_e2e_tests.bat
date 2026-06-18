@echo off

echo ============================================
echo   Fortuna Lotto - E2E Test Runner
echo ============================================

set "TEST_DIR=%~dp0"

if "%TEST_DIR:~-1%"=="\" set "TEST_DIR=%TEST_DIR:~0,-1%"
set "PROJECT_DIR=%TEST_DIR%\..\.."
set "TOMCAT_DIR=%PROJECT_DIR%\deploy\apache-tomcat-10.1.28"
set "E2E_SRC_DIR=%TEST_DIR%\src\e2e\java"
set "BUILD_DIR=%TEST_DIR%\build"
set "E2E_CLASSES_DIR=%BUILD_DIR%\e2e-classes"
set "TEST_LIB_DIR=%TEST_DIR%\test-lib"

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

call "%TOMCAT_DIR%\bin\setenv.bat" 2>nul
if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME is not set.
    echo Please set JAVA_HOME in deploy\apache-tomcat-10.1.28\bin\setenv.bat
    exit /b 1
)

echo JAVA_HOME: %JAVA_HOME%
echo.

echo [0/3] Checking if Tomcat is running...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/Lottery/ >nul 2>&1
if errorlevel 1 (
    echo WARNING: Tomcat may not be running at http://localhost:8080/Lottery
    echo          Tests may fail. Start with build_deploy_lottery.bat first.
    echo.
)

echo [0.5/3] Seeding test data...
sqlcmd -S localhost -U lux -P "No64Adonis*" -d master -i "%TEST_DIR%\seed_test_data.sql" >nul 2>&1
if errorlevel 1 (
    echo WARNING: Database seeding failed. Tests may fail due to stale data.
) else (
    echo Database seeded successfully.
)
echo.

echo [1/3] Compiling E2E test sources...
if exist "%E2E_CLASSES_DIR%" rmdir /s /q "%E2E_CLASSES_DIR%"
mkdir "%E2E_CLASSES_DIR%"

set "E2E_SOURCES=%BUILD_DIR%\e2e-sources.txt"
if exist "%E2E_SOURCES%" del "%E2E_SOURCES%"
for /r "%E2E_SRC_DIR%" %%f in (*.java) do (
    echo %%f>>"%E2E_SOURCES%"
)

set "COMPILE_CP=%TEST_LIB_DIR%\*"
"%JAVA_HOME%\bin\javac" -d "%E2E_CLASSES_DIR%" -cp "%COMPILE_CP%" @"%E2E_SOURCES%"
if errorlevel 1 (
    echo.
    echo COMPILATION FAILED! Fix errors above and retry.
    exit /b 1
)
echo Compilation successful.

set /a count=0
for /r "%E2E_CLASSES_DIR%" %%f in (Sec*E2ETest.class) do set /a count+=1
echo Found %count% test classes.

echo.
echo [2/3] Running E2E tests...
echo ============================================
set "RUN_CP=%E2E_CLASSES_DIR%;%TEST_LIB_DIR%\*"
"%JAVA_HOME%\bin\java" ^
    -cp "%RUN_CP%" ^
    org.junit.platform.console.ConsoleLauncher ^
    --scan-classpath="%E2E_CLASSES_DIR%" ^
    --details=verbose

echo.
echo [3/3] Test run complete.
echo ============================================
