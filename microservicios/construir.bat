@echo off
rem =====================================================================
rem  BUILD + TEST de los microservicios (Maven, sin Docker)
rem  Compila, ejecuta las pruebas, genera cobertura JaCoCo y crea los JAR.
rem  Uso:   microservicios\construir.bat
rem =====================================================================
setlocal
call "%~dp0..\devops\entorno.bat" silencioso || exit /b 1
cd /d "%~dp0"
echo.
echo === BUILD + TEST: ms-inventario y ms-facturacion ===
echo.
call mvn -B -f pom.xml clean verify
if errorlevel 1 goto :fallo
echo.
echo ============================================================
echo  [OK] Pruebas superadas y ejecutables creados:
echo    %~dp0ms-inventario\target\ms-inventario.jar
echo    %~dp0ms-facturacion\target\ms-facturacion.jar
echo.
echo  Reportes de cobertura (abrir en el navegador):
echo    %~dp0ms-inventario\target\site\jacoco\index.html
echo    %~dp0ms-facturacion\target\site\jacoco\index.html
echo ============================================================
exit /b 0

:fallo
echo.
echo [FALLO] El build o alguna prueba fallo. Revise el error de arriba.
exit /b 1
