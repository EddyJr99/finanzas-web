@echo off
rem Compila y arranca la aplicacion web (Tomcat embebido). No necesita Maven ni Tomcat.
rem Requisitos: JDK 11 o superior (java y javac en el PATH) y MySQL iniciado.
rem Uso: ejecutar_web.bat [puerto]   (por defecto 8080)
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo No se encontro javac. Instale un JDK 11 o superior y vuelva a intentar.
    pause
    exit /b 1
)

if exist out rmdir /s /q out
if not exist out mkdir out
javac -encoding UTF-8 -sourcepath src\main\java -cp "lib\*" -d out src\main\java\co\edu\sena\finanzas\servidor\ServidorWeb.java src\main\java\co\edu\sena\finanzas\servlet\MovimientoServlet.java src\main\java\co\edu\sena\finanzas\servlet\Html.java
if errorlevel 1 (
    echo Error al compilar.
    pause
    exit /b 1
)

echo.
echo Iniciando el servidor... (MySQL debe estar iniciado)
java -Dfile.encoding=UTF-8 -cp "out;lib\*" co.edu.sena.finanzas.servidor.ServidorWeb %1
echo.
pause
