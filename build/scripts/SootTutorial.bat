@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  SootTutorial startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Add default JVM options here. You can also use JAVA_OPTS and SOOT_TUTORIAL_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\SootTutorial.jar;%APP_HOME%\lib\soot-infoflow-cmd-classes.jar;%APP_HOME%\lib\soot-infoflow-classes.jar;%APP_HOME%\lib\soot-infoflow-summaries-classes.jar;%APP_HOME%\lib\trove4j-3.0.3.jar;%APP_HOME%\lib\soot-4.2.1.jar;%APP_HOME%\lib\slf4j-nop-1.7.5.jar;%APP_HOME%\lib\gs-ui-1.3.jar;%APP_HOME%\lib\gs-algo-1.3.jar;%APP_HOME%\lib\gs-core-1.3.jar;%APP_HOME%\lib\log4j-1.2.15.jar;%APP_HOME%\lib\dexlib2-2.4.0.jar;%APP_HOME%\lib\heros-1.2.2.jar;%APP_HOME%\lib\guava-27.1-android.jar;%APP_HOME%\lib\commons-io-2.6.jar;%APP_HOME%\lib\asm-util-8.0.1.jar;%APP_HOME%\lib\asm-commons-8.0.1.jar;%APP_HOME%\lib\asm-analysis-8.0.1.jar;%APP_HOME%\lib\asm-tree-8.0.1.jar;%APP_HOME%\lib\asm-8.0.1.jar;%APP_HOME%\lib\xmlpull-1.1.3.4d_b4_min.jar;%APP_HOME%\lib\axml-2.0.0.jar;%APP_HOME%\lib\polyglot-2006.jar;%APP_HOME%\lib\jasmin-3.0.2.jar;%APP_HOME%\lib\slf4j-api-1.7.5.jar;%APP_HOME%\lib\javax.annotation-api-1.3.2.jar;%APP_HOME%\lib\jaxb-runtime-2.4.0-b180830.0438.jar;%APP_HOME%\lib\jaxb-api-2.4.0-b180830.0359.jar;%APP_HOME%\lib\junit-4.12.jar;%APP_HOME%\lib\pherd-1.0.jar;%APP_HOME%\lib\mbox2-1.0.jar;%APP_HOME%\lib\scala-library-2.10.1.jar;%APP_HOME%\lib\mail-1.4.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\error_prone_annotations-2.2.0.jar;%APP_HOME%\lib\j2objc-annotations-1.1.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.17.jar;%APP_HOME%\lib\functionaljava-4.2.jar;%APP_HOME%\lib\java_cup-0.9.2.jar;%APP_HOME%\lib\javax.activation-api-1.2.0.jar;%APP_HOME%\lib\txw2-2.4.0-b180830.0438.jar;%APP_HOME%\lib\istack-commons-runtime-3.0.7.jar;%APP_HOME%\lib\stax-ex-1.8.jar;%APP_HOME%\lib\FastInfoset-1.2.15.jar;%APP_HOME%\lib\hamcrest-core-1.3.jar;%APP_HOME%\lib\commons-math-2.1.jar;%APP_HOME%\lib\commons-math3-3.4.1.jar;%APP_HOME%\lib\jfreechart-1.0.14.jar;%APP_HOME%\lib\activation-1.1.jar;%APP_HOME%\lib\checker-compat-qual-2.5.2.jar;%APP_HOME%\lib\jcommon-1.0.17.jar;%APP_HOME%\lib\xml-apis-1.3.04.jar;%APP_HOME%\lib\itext-2.1.5.jar;%APP_HOME%\lib\bcmail-jdk14-138.jar;%APP_HOME%\lib\bcprov-jdk14-138.jar

@rem Execute SootTutorial
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %SOOT_TUTORIAL_OPTS%  -classpath "%CLASSPATH%" dev.navids.soottutorial.Main %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable SOOT_TUTORIAL_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%SOOT_TUTORIAL_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
