@echo off

echo ============================================
echo   Fortuna Lotto - Unit Test Runner
echo ============================================

set "PROJECT_DIR=%cd%"
set "TOMCAT_DIR=%PROJECT_DIR%\deploy\apache-tomcat-10.1.28"
set "SRC_DIR=%PROJECT_DIR%\src\main\java"
set "TEST_SRC_DIR=%PROJECT_DIR%\tests\unit_test\java"
set "BUILD_DIR=%PROJECT_DIR%\build"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "TEST_CLASSES_DIR=%BUILD_DIR%\test-classes"
set "TEST_LIB_DIR=%PROJECT_DIR%\tests\automation_test\test-lib"
set "WEB_LIB_DIR=%PROJECT_DIR%\src\main\webapp\WEB-INF\lib"

echo Initializing environment...
call "%TOMCAT_DIR%\bin\setenv.bat" 2>nul

if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME is not set.
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%

set "COMPILE_CP=%CLASSES_DIR%;%WEB_LIB_DIR%\*;%TEST_LIB_DIR%\*;%TOMCAT_DIR%\lib\servlet-api.jar;%TOMCAT_DIR%\lib\jsp-api.jar"
set "RUN_CP=%TEST_CLASSES_DIR%;%CLASSES_DIR%;%WEB_LIB_DIR%\*;%TEST_LIB_DIR%\*;%TOMCAT_DIR%\lib\servlet-api.jar;%TOMCAT_DIR%\lib\jsp-api.jar"

echo.
echo [1/3] Compiling main sources...
if exist "%CLASSES_DIR%" rmdir /s /q "%CLASSES_DIR%"
mkdir "%CLASSES_DIR%"

set "MAIN_SOURCES=%BUILD_DIR%\main-sources.txt"
if exist "%MAIN_SOURCES%" del "%MAIN_SOURCES%"
for /r "%SRC_DIR%" %%f in (*.java) do (
    echo %%f>>"%MAIN_SOURCES%"
)

"%JAVA_HOME%\bin\javac" -d "%CLASSES_DIR%" ^
    -cp "%WEB_LIB_DIR%\*;%TOMCAT_DIR%\lib\servlet-api.jar;%TOMCAT_DIR%\lib\jsp-api.jar" ^
    @"%MAIN_SOURCES%"

if %errorlevel% neq 0 (
    echo Error: Main source compilation failed.
    exit /b 1
)
echo Main sources compiled successfully.

echo.
echo [2/3] Compiling test sources...
if exist "%TEST_CLASSES_DIR%" rmdir /s /q "%TEST_CLASSES_DIR%"
mkdir "%TEST_CLASSES_DIR%"

set "TEST_SOURCES=%BUILD_DIR%\test-sources.txt"
if exist "%TEST_SOURCES%" del "%TEST_SOURCES%"
for /r "%TEST_SRC_DIR%" %%f in (*.java) do (
    echo %%f>>"%TEST_SOURCES%"
)

if not exist "%TEST_SOURCES%" (
    echo Error: No test source files found.
    exit /b 1
)

"%JAVA_HOME%\bin\javac" -d "%TEST_CLASSES_DIR%" ^
    -cp "%COMPILE_CP%" ^
    @"%TEST_SOURCES%"

if %errorlevel% neq 0 (
    echo Error: Test source compilation failed.
    exit /b 1
)
echo Test sources compiled successfully.

echo.
echo [3/3] Running tests...
echo ============================================
"%JAVA_HOME%\bin\java" ^
    -Dnet.bytebuddy.experimental=true ^
    -javaagent:"%TEST_LIB_DIR%\byte-buddy-agent-1.14.12.jar" ^
    -cp "%RUN_CP%" ^
    org.junit.platform.console.ConsoleLauncher ^
    --scan-classpath="%TEST_CLASSES_DIR%" ^
    --details=verbose

set TEST_RESULT=%errorlevel%

echo.
echo ============================================
if %TEST_RESULT% equ 0 (
    echo   ALL TESTS PASSED!
) else (
    echo   SOME TESTS FAILED - see output above
)
echo ============================================

exit /b %TEST_RESULT%
