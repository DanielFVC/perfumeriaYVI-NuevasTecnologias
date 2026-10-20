@echo off
rem =====================================================================
rem  DEPLOY de los microservicios como software (sin Docker)
rem  Copia los JAR a la carpeta "servidor" y arranca cada uno en su ventana.
rem
rem  Uso:   microservicios\desplegar.bat            (usa los JAR de construir.bat)
rem         microservicios\desplegar.bat release    (descarga la ultima version de GitHub Releases)
rem
rem  Carpeta servidor: %USERPROFILE%\PerfumeriaYVI-servidor
rem  Cierre el vigilante antes de desplegar.
rem =====================================================================
setlocal
call "%~dp0..\devops\entorno.bat" silencioso || exit /b 1
set "SERVIDOR=%USERPROFILE%\PerfumeriaYVI-servidor"
set "REPO=DanielFVC/perfumeriaYVI-NuevasTecnologias"

if not exist "%SERVIDOR%\ms-inventario" mkdir "%SERVIDOR%\ms-inventario"
if not exist "%SERVIDOR%\ms-facturacion" mkdir "%SERVIDOR%\ms-facturacion"

echo.
echo === DEPLOY de microservicios en %SERVIDOR% ===
echo 1. Deteniendo la version anterior...
call "%~dp0detener.bat" silencioso
timeout /t 2 /nobreak >nul

if /i "%~1"=="release" goto :desde_release

echo 2. Copiando los JAR construidos localmente...
if not exist "%~dp0ms-inventario\target\ms-inventario.jar" goto :sin_jar
if not exist "%~dp0ms-facturacion\target\ms-facturacion.jar" goto :sin_jar
copy /y "%~dp0ms-inventario\target\ms-inventario.jar" "%SERVIDOR%\ms-inventario\" >nul || goto :fallo_copia
copy /y "%~dp0ms-facturacion\target\ms-facturacion.jar" "%SERVIDOR%\ms-facturacion\" >nul || goto :fallo_copia
goto :arrancar

:desde_release
echo 2. Descargando la ultima version publicada en GitHub Releases...
curl -fL -o "%SERVIDOR%\ms-inventario\ms-inventario.jar" "https://github.com/%REPO%/releases/latest/download/ms-inventario.jar" || goto :sin_release
curl -fL -o "%SERVIDOR%\ms-facturacion\ms-facturacion.jar" "https://github.com/%REPO%/releases/latest/download/ms-facturacion.jar" || goto :sin_release

:arrancar
echo 3. Arrancando los servicios, cada uno en su propia ventana...
start "ms-inventario (8091)" /D "%SERVIDOR%\ms-inventario" java -jar ms-inventario.jar
start "ms-facturacion (8092)" /D "%SERVIDOR%\ms-facturacion" java -jar ms-facturacion.jar
echo 4. Verificando la salud de los servicios...
call "%~dp0estado.bat" esperar
exit /b %errorlevel%

:fallo_copia
echo [ERROR] No se pudo copiar el JAR. Cierre el vigilante y las ventanas de los servicios y repita.
exit /b 1

:sin_jar
echo [ERROR] No estan los JAR. Ejecute primero: microservicios\construir.bat
exit /b 1

:sin_release
echo [ERROR] No se pudo descargar de GitHub Releases.
echo         Revise que exista una version publicada y que el repositorio sea publico.
exit /b 1
