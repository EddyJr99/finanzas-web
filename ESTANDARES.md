# Estándares de codificación aplicados

| Elemento | Regla | Ejemplos en el proyecto |
|---|---|---|
| Paquetes | Minúsculas, sin guiones bajos, dominio invertido | `co.edu.sena.finanzas.servlet`, `co.edu.sena.finanzas.dao` |
| Clases | PascalCase, sustantivo | `MovimientoServlet`, `FormularioMovimiento`, `OpcionCatalogo` |
| Métodos | camelCase, empiezan con verbo | `doGet`, `guardar`, `eliminar`, `cargarCatalogos` |
| Variables y atributos | camelCase, nombre descriptivo | `movimientoDAO`, `idMovimiento`, `textoBuscado` |
| Constantes | MAYÚSCULAS con guion bajo | `VISTA_LISTAR`, `ACCION_CREAR` |
| Archivos JSP | minúsculas, nombre de la vista | `listar.jsp`, `formulario.jsp`, `buscar.jsp` |
| Parámetros de formularios | camelCase igual al atributo del modelo | `montoTotal`, `idCuenta` |

Otras prácticas:

- Arquitectura por capas: el servlet (controlador) recibe la solicitud, usa el DAO y entrega los datos a un JSP (vista) con `RequestDispatcher.forward`. Las páginas JSP quedan dentro de `WEB-INF/vistas`, por lo que solo se pueden abrir a través del servlet.
- `doGet` solo lee datos; `doPost` es el único que modifica la base de datos.
- Patrón Post-Redirect-Get: después de crear, actualizar o eliminar se redirige a la lista, para que recargar la página no repita la operación.
- Validación de todos los datos en el servidor (`FormularioMovimiento`), además de la validación HTML del navegador.
- Todo texto que escribe el usuario se escapa antes de mostrarlo (`Html.escapar`) para evitar inyección de código HTML (XSS).
- `PreparedStatement` en todas las consultas y transacciones en las operaciones que tocan dos tablas (clases del módulo de la evidencia AA2-EV01).
- Comentarios Javadoc en clases y métodos públicos.
