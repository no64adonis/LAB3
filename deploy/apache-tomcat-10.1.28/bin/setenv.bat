@echo off
rem Set environment variables for Tomcat

rem Set CATALINA_HOME to the current directory (where this script is located)
set "CATALINA_HOME=%~dp0.."

rem Remove trailing backslash if present
if "%CATALINA_HOME:~-1%"=="\" set "CATALINA_HOME=%CATALINA_HOME:~0,-1%"

rem Set CATALINA_BASE to the same as CATALINA_HOME if not already set
if "%CATALINA_BASE%"=="" set "CATALINA_BASE=%CATALINA_HOME%"

rem Set JAVA_HOME to the JDK installation directory
set "JAVA_HOME=C:\Program Files\Java\jdk-24"
set "JRE_HOME=%JAVA_HOME%"

echo CATALINA_HOME is set to: %CATALINA_HOME%
echo CATALINA_BASE is set to: %CATALINA_BASE%
echo JAVA_HOME is set to: %JAVA_HOME%
echo JRE_HOME is set to: %JRE_HOME%
