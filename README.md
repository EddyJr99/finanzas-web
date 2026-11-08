# Módulo Movimientos web (servlets y JSP) - SGFP

Evidencia GA7-220501096-AA2-EV02. Aprendiz: Eddy Alexander Altamar Angulo (Ficha 3336221, ADSO, SENA).

Aplicación web en Java con **servlets** y **JSP** para gestionar los movimientos del Sistema de Gestión de Finanzas Personales. Se conecta por JDBC a la base MySQL `sgfp` (la misma de la evidencia AA2-EV01) y permite listar, buscar, registrar, actualizar y eliminar movimientos.

## Qué pide la guía y dónde está

| Requisito | Dónde se cumple |
|---|---|
| Formularios HTML con servlets | `formulario.jsp` y `buscar.jsp` envían sus datos a `MovimientoServlet` (URL `/movimientos`) |
| Métodos GET y POST | `doGet`: listar, buscar y mostrar formularios. `doPost`: crear, actualizar y eliminar |
| Elementos de JSP | Directivas `page` e `include`, declaraciones `<%! %>`, scriptlets `<% %>`, expresiones `<%= %>`, comentarios `<%-- --%>`, acciones `<jsp:include>`, objeto implícito `exception` en `error.jsp` y lenguaje de expresiones `${ }` |
| Artefactos del ciclo del software | Reutiliza el modelo `Movimiento` y los DAO diseñados para el módulo Movimientos |
| Versionamiento | Proyecto en Git/GitHub (enlace en `ENLACE_REPOSITORIO.txt`) |

## Cómo ejecutarlo (en cualquier equipo)

### Requisitos

- JDK 11 o superior (que `java -version` y `javac -version` funcionen). Probado con JDK 17.
- MySQL 8.x iniciado (Laragon, XAMPP o MySQL Server) en el puerto 3306.

No hace falta instalar Maven ni Tomcat: el servidor Tomcat 9 viaja embebido en la carpeta `lib/`, junto con el driver JDBC de MySQL.

### Paso 1. Crear la base de datos

Ejecutar `sql/sgfp_completo.sql`, que crea la base `sgfp`, sus seis tablas y datos de ejemplo. Se puede ejecutar más de una vez sin borrar nada. Si ya existe la base `sgfp` de la evidencia anterior, este paso no es necesario.

- Con DBeaver: abrir el archivo en el editor SQL y ejecutarlo como script (Alt + X).
- Con la consola: `mysql -u root -p < sql/sgfp_completo.sql`

### Paso 2. Revisar la conexión

Por defecto se usa el usuario `root` sin clave en `localhost:3306` (configuración típica de Laragon y XAMPP). Si su MySQL tiene otra clave, edite `conexion.properties`.

### Paso 3. Arrancar el servidor

- Windows: doble clic en `ejecutar_web.bat`.
- Linux o macOS: `./ejecutar_web.sh`

El script compila el código, inicia Tomcat y abre el navegador en `http://localhost:8080/finanzas/`. Si el puerto 8080 está ocupado, usa el siguiente libre y lo muestra en la consola; también se puede indicar uno: `ejecutar_web.bat 9090`. Para detener el servidor, cierre la ventana negra.

## Páginas de la aplicación

| URL | Método | Qué hace |
|---|---|---|
| `/finanzas/` | GET | Página de inicio con el menú (`index.jsp`) |
| `/finanzas/movimientos` | GET | Lista todos los movimientos |
| `/finanzas/movimientos?accion=buscar&id=5` | GET | Busca un movimiento por id |
| `/finanzas/movimientos?accion=nuevo` | GET | Formulario para registrar |
| `/finanzas/movimientos?accion=editar&id=5` | GET | Formulario para editar |
| `/finanzas/movimientos` con `accion=crear` | POST | Inserta un movimiento (también en `detalle_categoria`) |
| `/finanzas/movimientos` con `accion=actualizar` | POST | Actualiza un movimiento |
| `/finanzas/movimientos` con `accion=eliminar` | POST | Elimina un movimiento |

## Si algo falla

| Mensaje | Qué hacer |
|---|---|
| Página "Error de base de datos: Communications link failure" | MySQL no está iniciado o usa otro puerto. Inícielo y revise `conexion.properties`. |
| `Access denied for user 'root'` | Escriba la clave de MySQL en `conexion.properties`. |
| `Unknown database 'sgfp'` | Falta ejecutar `sql/sgfp_completo.sql` (Paso 1). |
| La lista de cuentas, medios de pago o categorías sale vacía | Ejecutar `sql/sgfp_completo.sql`, que inserta datos de ejemplo. |
| `No se encontro javac` | Instalar un JDK (no solo el JRE) y reiniciar la terminal. |
| El puerto está ocupado | El programa prueba el siguiente puerto libre; mire la dirección que imprime. |

## Estructura

```
src/main/java/co/edu/sena/finanzas/
  servlet/    MovimientoServlet, FormularioMovimiento, OpcionCatalogo, Html
  servidor/   ServidorWeb (Tomcat embebido)
  modelo/     Movimiento
  conexion/   ConexionBD
  dao/        MovimientoDAO, CatalogoDAO
src/main/webapp/
  index.jsp                     página de inicio
  css/estilo.css                estilos
  WEB-INF/web.xml               configuración del servlet y de las páginas de error
  WEB-INF/vistas/               listar.jsp, buscar.jsp, formulario.jsp, error.jsp
  WEB-INF/jspf/                 encabezado, pie y mensajes reutilizables
sql/          sgfp_completo.sql
lib/          Tomcat 9.0.123 (jars) y mysql-connector-j-8.4.0.jar
conexion.properties   ejecutar_web.bat   ejecutar_web.sh
ESTANDARES.md
```

Los estándares de codificación están en `ESTANDARES.md`.

Los jars de `lib/` son Apache Tomcat 9.0.123 (licencia Apache 2.0, texto en `lib/LICENSE-Apache-Tomcat.txt`) y MySQL Connector/J 8.4.0 (GPLv2 con la excepción FOSS de Oracle). Se incluyen solo para facilitar la ejecución.
