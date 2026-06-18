@echo off

echo Building and deploying the Lottery application...

set "PROJECT_DIR=%cd%"
set "TOMCAT_DIR=%PROJECT_DIR%\deploy\apache-tomcat-10.1.28"
set "WAR_DIR=%PROJECT_DIR%\deploy\war"
set "WAR_FILE=%WAR_DIR%\Lottery.war"
set "BUILD_DIR=%PROJECT_DIR%\build"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "SRC_DIR=%PROJECT_DIR%\src\main\java"
set "SOURCES_FILE=%BUILD_DIR%\sources.txt"

echo Project directory: %PROJECT_DIR%
echo Tomcat directory: %TOMCAT_DIR%

echo Initializing Tomcat environment...
call "%TOMCAT_DIR%\bin\setenv.bat"

echo Checking for JDK...
if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME is not set. Please set JAVA_HOME to your JDK installation directory.
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%

echo Cleaning previous build...
if exist "%BUILD_DIR%" (
    rmdir /s /q "%BUILD_DIR%"
)
mkdir "%BUILD_DIR%"
mkdir "%CLASSES_DIR%"
if exist "%SOURCES_FILE%" del "%SOURCES_FILE%"

echo Cleaning previous WAR directory...
if exist "%WAR_DIR%\Lottery" (
    rmdir /s /q "%WAR_DIR%\Lottery"
)

echo Compiling Java source files...
for /r "%SRC_DIR%" %%f in (*.java) do (
    echo %%f>>"%SOURCES_FILE%"
)
if not exist "%SOURCES_FILE%" (
    echo Error: No Java source files found to compile.
    exit /b 1
)
"%JAVA_HOME%\bin\javac" -d "%CLASSES_DIR%" ^
    -cp "%TOMCAT_DIR%\lib\servlet-api.jar;%TOMCAT_DIR%\lib\jsp-api.jar;%PROJECT_DIR%\src\main\webapp\WEB-INF\lib\*" ^
    @"%SOURCES_FILE%"

if %errorlevel% neq 0 (
    echo Error: Java compilation failed.
    exit /b 1
)

echo Java compilation completed successfully.

echo Creating WAR directory structure...
mkdir "%WAR_DIR%\Lottery"
mkdir "%WAR_DIR%\Lottery\WEB-INF"
mkdir "%WAR_DIR%\Lottery\WEB-INF\classes"
mkdir "%WAR_DIR%\Lottery\WEB-INF\lib"
mkdir "%WAR_DIR%\Lottery\META-INF"
mkdir "%WAR_DIR%\Lottery\error"

echo Copying compiled classes and resources...
xcopy /E /I /Y "%CLASSES_DIR%\com" "%WAR_DIR%\Lottery\WEB-INF\classes\com" > nul
if exist "%PROJECT_DIR%\src\main\resources" (
    xcopy /E /I /Y "%PROJECT_DIR%\src\main\resources\*" "%WAR_DIR%\Lottery\WEB-INF\classes\" > nul
)

echo Copying web resources...
xcopy /E /I /Y "%PROJECT_DIR%\src\main\webapp\*.jsp" "%WAR_DIR%\Lottery\" > nul
xcopy /E /I /Y "%PROJECT_DIR%\src\main\webapp\*.html" "%WAR_DIR%\Lottery\" > nul
xcopy /E /I /Y "%PROJECT_DIR%\src\main\webapp\error" "%WAR_DIR%\Lottery\error" > nul
xcopy /E /I /Y "%PROJECT_DIR%\src\main\webapp\assets" "%WAR_DIR%\Lottery\assets" > nul
xcopy /Y "%PROJECT_DIR%\src\main\webapp\META-INF\MANIFEST.MF" "%WAR_DIR%\Lottery\META-INF\" > nul

echo Copying WEB-INF resources...
xcopy /Y "%PROJECT_DIR%\src\main\webapp\WEB-INF\web.xml" "%WAR_DIR%\Lottery\WEB-INF\" > nul
xcopy /E /I /Y "%PROJECT_DIR%\src\main\webapp\WEB-INF\lib" "%WAR_DIR%\Lottery\WEB-INF\lib" > nul

echo Creating WAR file...
cd /d "%WAR_DIR%"
"%JAVA_HOME%\bin\jar" -cvf Lottery.war -C Lottery .

if %errorlevel% neq 0 (
    echo Error: Failed to create WAR file.
    exit /b 1
)

echo WAR file created successfully at %WAR_FILE%

echo Deploying application to Tomcat...

echo Stopping Tomcat server if it's running...
call "%TOMCAT_DIR%\bin\shutdown.bat"
timeout /t 5 /nobreak > nul

echo Killing any remaining Java processes...
taskkill /F /IM java.exe 2>nul

echo Cleaning previous deployment...
if exist "%TOMCAT_DIR%\webapps\Lottery" (
    rmdir /s /q "%TOMCAT_DIR%\webapps\Lottery"
)
if exist "%TOMCAT_DIR%\webapps\Lottery.war" (
    del "%TOMCAT_DIR%\webapps\Lottery.war"
)

echo Copying WAR file to Tomcat webapps directory...
copy /Y "%WAR_FILE%" "%TOMCAT_DIR%\webapps\"

if %errorlevel% neq 0 (
    echo Error: Failed to copy WAR file to Tomcat.
    exit /b 1
)

echo Starting Tomcat server...
call "%TOMCAT_DIR%\bin\startup.bat"

if %errorlevel% neq 0 (
    echo Error: Failed to start Tomcat server.
    exit /b 1
)

echo Waiting for application to deploy...
timeout /t 10 /nobreak > nul

echo.
echo [SUCCESS] Lottery application built and deployed successfully!
echo You can access the application at: http://localhost:8080/Lottery/

exit /b 0
