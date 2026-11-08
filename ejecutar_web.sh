#!/bin/sh
# Compila y arranca la aplicacion web (Tomcat embebido). No necesita Maven ni Tomcat.
# Requisitos: JDK 11 o superior (java y javac en el PATH) y MySQL iniciado.
# Uso: ./ejecutar_web.sh [puerto]   (por defecto 8080)
cd "$(dirname "$0")" || exit 1

if ! command -v javac >/dev/null 2>&1; then
    echo "No se encontro javac. Instale un JDK 11 o superior y vuelva a intentar."
    exit 1
fi

rm -rf out
mkdir -p out
javac -encoding UTF-8 -sourcepath src/main/java -cp "lib/*" -d out \
    src/main/java/co/edu/sena/finanzas/servidor/ServidorWeb.java \
    src/main/java/co/edu/sena/finanzas/servlet/MovimientoServlet.java \
    src/main/java/co/edu/sena/finanzas/servlet/Html.java || exit 1

echo
echo "Iniciando el servidor... (MySQL debe estar iniciado)"
java -Dfile.encoding=UTF-8 -cp "out:lib/*" co.edu.sena.finanzas.servidor.ServidorWeb "$@"
