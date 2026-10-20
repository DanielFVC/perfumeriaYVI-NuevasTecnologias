@echo off
rem =====================================================================
rem  OPERATE: vigilante de los microservicios (recuperacion automatica)
rem  Cada 10 segundos revisa /actuator/health. Si un servicio se cae,
rem  lo vuelve a arrancar solo y lo anota en vigilante.log.
rem  Es lo mismo que hace "restart: unless-stopped" en Docker, pero para software.
rem  Uso:   microservicios\vigilante.bat      (Ctrl+C o cerrar la ventana para detenerlo)
rem =====================================================================
setlocal
title Vigilante de microservicios - Perfumeria YVI
call "%~dp0..\devops\entorno.bat" silencioso || exit /b 1
set "SERVIDOR=%USERPROFILE%\PerfumeriaYVI-servidor"
echo.
echo  Vigilando ms-inventario (8091) y ms-facturacion (8092) cada 10 segundos.
echo  Registro: %SERVIDOR%\vigilante.log
echo.
:ciclo
call :vigilar ms-inventario 8091
call :vigilar ms-facturacion 8092
timeout /t 10 /nobreak >nul
goto :ciclo

:vigilar
curl -s -f --max-time 3 http://localhost:%2/actuator/health >nul 2>&1
if not errorlevel 1 exit /b 0
rem Segunda revision 3 s despues: evita reiniciar un servicio que esta arrancando
timeout /t 3 /nobreak >nul
curl -s -f --max-time 3 http://localhost:%2/actuator/health >nul 2>&1
if not errorlevel 1 exit /b 0
if not exist "%SERVIDOR%\%1\%1.jar" (
  echo  [%time:~0,8%] %1 no esta desplegado. Ejecute desplegar.bat
  exit /b 0
)
echo  [%time:~0,8%] %1 CAIDO - reiniciando automaticamente...
>> "%SERVIDOR%\vigilante.log" echo %date% %time:~0,8% %1 caido, reiniciado por el vigilante
start "%1 (%2)" /D "%SERVIDOR%\%1" java -jar %1.jar
timeout /t 20 /nobreak >nul
curl -s -f --max-time 3 http://localhost:%2/actuator/health >nul 2>&1
if not errorlevel 1 echo  [%time:~0,8%] %1 recuperado
exit /b 0
