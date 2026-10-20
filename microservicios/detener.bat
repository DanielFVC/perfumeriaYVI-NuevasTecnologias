@echo off
rem =====================================================================
rem  Detiene los microservicios (busca el proceso que escucha en cada puerto;
rem  funciona con Windows en espanol o en ingles)
rem  Uso:   microservicios\detener.bat
rem =====================================================================
setlocal
set "HABIA="
for %%P in (8091 8092) do (
  for /f "tokens=5" %%I in ('netstat -ano -p tcp ^| findstr /r /c:":%%P  *0\.0\.0\.0:0 "') do (
    taskkill /PID %%I /F >nul 2>&1 && echo    Servicio del puerto %%P detenido, proceso %%I
    set "HABIA=1"
  )
)
if /i "%~1"=="silencioso" exit /b 0
if not defined HABIA echo    No habia microservicios en ejecucion.
exit /b 0
