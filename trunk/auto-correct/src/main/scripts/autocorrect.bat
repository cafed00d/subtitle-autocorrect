@echo off
rem ===========================================================================
rem
rem  This script is used to run the Subtitle Auto-Correct utility.
rem
rem  Usage:
rem     autocorrect [-<options>] <srt-file(s)>
rem
rem  Where
rem     <options> is one or more of the following:
rem       a - generate log file showing words auto corrected
rem       v - generate additional console output
rem       q - generate no console output
rem     <srt-file(s)> is one or more SRT files to process
rem
rem ===========================================================================
setlocal

REM The app's JAR file should be in the same directory as this script
set DIRNAME=%~dp0%
REM The app's directory is first in the classpath so that the properties file
REM and log4j.xml file can be placed there to override the defaults.
set CLASSPATH=%DIRNAME%
set CLASSPATH=%CLASSPATH%;%DIRNAME%${project.artifactId}-${project.version}.jar

REM The third-party JARs should be in the lib subdirectory
set LIBDIR=%DIRNAME%lib
set CLASSPATH=%CLASSPATH%;%LIBDIR%\commons-logging.jar
set CLASSPATH=%CLASSPATH%;%LIBDIR%\log4j.jar

REM If JAVA_HOME is set, use that, otherwise hope that java is in the PATH
IF "%JAVA_HOME%" == "" (
  set JAVA=java
) ELSE (
  set JAVA=%JAVA_HOME%\bin\java
)

REM Run the app
"%JAVA%" -classpath %CLASSPATH% org.cafed00d.subtitle.AutoCorrect %* 
