@echo off
rem =====================================================================
rem  OPERATE: revisa la salud de los microservicios y de la aplicacion web
rem  Uso:   microservicios\estado.bat
rem =====================================================================
setlocal
set "INTENTOS=1"
if /i "%~1"=="esperar" set "INTENTOS=40"
set "TODOS=0"
echo.
call :revisar ms-inventario 8091
call :revisar ms-facturacion 8092
curl -s -f -o nul --max-time 3 http://localhost:8080/login
if errorlevel 1 (echo    [--]     aplicacion web no responde en http://localhost:8080 - se levanta con docker compose) else (echo    [ARRIBA] aplicacion web    http://localhost:8080)
echo.
exit /b %TODOS%

:revisar
set /a N=0
:reintentar
curl -s -f --max-time 3 http://localhost:%2/actuator/health 2>nul | findstr /c:"UP" >nul
if not errorlevel 1 goto :arriba
set /a N+=1
if %N% lss %INTENTOS% (
  timeout /t 2 /nobreak >nul
  goto :reintentar
)
echo    [CAIDO]  %1  no responde en http://localhost:%2
set "TODOS=1"
exit /b 1
:arriba
echo    [ARRIBA] %1  http://localhost:%2/actuator/health
exit /b 0
