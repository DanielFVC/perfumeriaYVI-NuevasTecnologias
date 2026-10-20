@echo off
rem =====================================================================
rem  Perfumeria YVI - Prepara esta ventana de CMD con Java 21 y Maven
rem  Uso:   call devops\entorno.bat
rem  Usa el JDK 21 instalado y el Maven que trae NetBeans 22.
rem  Solo afecta a la ventana actual: no cambia nada del sistema.
rem =====================================================================
if exist "C:\Program Files\Java\jdk-21\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-21"
if exist "C:\Program Files\netbeans-22\netbeans\java\maven\bin\mvn.cmd" set "MAVEN_HOME=C:\Program Files\netbeans-22\netbeans\java\maven"
if not exist "C:\Program Files\Java\jdk-21\bin\java.exe" goto :sin_java
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"
if /i "%~1"=="silencioso" exit /b 0
echo.
echo  Java y Maven listos para esta ventana:
java -version 2>&1 | findstr /i "version"
if defined MAVEN_HOME call mvn -v 2>nul | findstr /b /c:"Apache Maven"
echo.
exit /b 0

:sin_java
echo [ERROR] No se encontro Java 21 en C:\Program Files\Java\jdk-21
exit /b 1
