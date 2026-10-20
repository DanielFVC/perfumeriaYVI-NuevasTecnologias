# Trazabilidad de requisitos – Perfumería YVI

Este archivo es el índice que une el documento IEEE 830 con el código.
Cada clase y método importante tiene un comentario con el requisito que implementa
(por ejemplo `RF04 (CU04, HU03)`), así se puede buscar cualquier código en el proyecto.

## Actores

| Actor | Descripción |
|---|---|
| Administrador | Rol `ADMIN`. Accede a todos los módulos, gestiona vendedores, permisos y auditoría. |
| Vendedor | Rol `VENDEDOR`. Accede solo a los módulos que el administrador le habilite. |

## Historias de usuario

| HU | Rol | Historia | CU | RF | RNF |
|---|---|---|---|---|---|
| HU01 | Administrador / Vendedor | Iniciar y cerrar sesión de forma segura | CU01, CU02 | RF01, RF02 | RNF-S1, RNF-S2, RNF-S3, RNF-S7 |
| HU02 | Administrador | Definir qué módulos puede usar cada vendedor | CU03, CU10 | RF03, RF10 | RNF-S4, RNF-S7 |
| HU03 | Vendedor | Registrar y mantener los datos de los clientes | CU04 | RF04 | RNF-F2, RNF-F3, RNF-U3 |
| HU04 | Vendedor | Gestionar los proveedores | CU05 | RF05 | RNF-F2, RNF-F3, RNF-U3 |
| HU05 | Vendedor | Controlar el inventario y ver alertas de stock bajo | CU06 | RF06 | RNF-F2, RNF-U2 |
| HU06 | Vendedor | Registrar ventas con varios productos, total automático y descuento de stock | CU07 | RF07 | RNF-F1, RNF-R1 |
| HU07 | Administrador / Vendedor | Consultar, corregir y anular ventas | CU08 | RF08 | RNF-F1, RNF-U2 |
| HU08 | Administrador | Crear, bloquear y activar vendedores | CU09 | RF09 | RNF-S1, RNF-S6 |
| HU09 | Administrador | Revisar el historial de auditoría y exportarlo | CU11, CU12 | RF11, RF12 | RNF-R2, RNF-S8 |

## Requisitos funcionales y casos de uso

| RF | CU | Nombre | Actores | Código principal |
|---|---|---|---|---|
| RF01 | CU01 | Iniciar sesión | Administrador, Vendedor | `AutenticacionService`, `LoginViewModel`, `LoginBinder` |
| RF02 | CU02 | Cerrar sesión | Administrador, Vendedor | `LoginBinder.cerrarSesion` |
| RF03 | CU03 | Controlar acceso por rol y permisos (incluido en CU04–CU12) | — | `AutorizacionInterceptor`, `@RequiereAcceso`, `UsuarioSesion` |
| RF04 | CU04 | Gestionar clientes | Administrador, Vendedor | `ClienteService`, `ClienteViewModel`, `ClienteBinder` |
| RF05 | CU05 | Gestionar proveedores | Administrador, Vendedor | `ProveedorService`, `ProveedorViewModel`, `ProveedorBinder` |
| RF06 | CU06 | Gestionar productos e inventario | Administrador, Vendedor | `ProductoService`, `ProductoViewModel`, `ProductoBinder` |
| RF07 | CU07 | Registrar venta con control de stock | Administrador, Vendedor | `VentaService.registrar`, `VentaViewModel`, `VentaBinder` |
| RF08 | CU08 | Consultar y administrar ventas | Administrador, Vendedor | `VentaService.actualizar/eliminar`, `VentaViewModel` |
| RF09 | CU09 | Gestionar vendedores | Administrador | `UsuarioService`, `UsuarioViewModel`, `UsuarioBinder` |
| RF10 | CU10 | Asignar permisos a vendedores | Administrador | `UsuarioService.actualizarPermisos` |
| RF11 | CU11 | Consultar auditoría | Administrador | `AuditoriaService`, `AuditoriaViewModel`, `AuditoriaBinder` |
| RF12 | CU12 | Exportar auditoría a Excel y PDF (extiende CU11) | Administrador | `ExportadorAuditoria` |

## Requisitos no funcionales

| RNF | Categoría | Requisito | Dónde se cumple |
|---|---|---|---|
| RNF-R1 | Rendimiento | Respuesta ≤ 2 s en el 95 % de las operaciones | Consultas indexadas, `JdbcTemplate`, pool HikariCP |
| RNF-R2 | Rendimiento | Auditoría paginada de 20 en 20 registros | `AuditoriaViewModel.REGISTROS_POR_PAGINA` |
| RNF-R3 | Rendimiento | Pool de máximo 10 conexiones a la BD | `application.properties` (`hikari.maximum-pool-size`) |
| RNF-S1 | Seguridad | Contraseñas solo como hash BCrypt (costo 10) | `CifradoContrasena`, columna `password_hash` |
| RNF-S2 | Seguridad | Bloqueo de 15 min tras 5 intentos fallidos | `AutenticacionService`, columnas `intentos_fallidos`, `bloqueado_hasta` |
| RNF-S3 | Seguridad | Sesión: expira a los 30 min, nuevo ID al iniciar sesión, cookie HttpOnly y SameSite | `LoginBinder`, `web.xml`, `context.xml`, `application.properties` |
| RNF-S4 | Seguridad | Protección CSRF y cambios solo por POST | `CsrfFiltro`, formularios JSP |
| RNF-S5 | Seguridad | Consultas parametrizadas, salida escapada en las vistas (XSS) y cabeceras de seguridad HTTP | Repositorios `Jdbc*`, `<c:out>`/`fn:escapeXml` en JSP, `CabecerasSeguridadFiltro` |
| RNF-S6 | Seguridad | Política de contraseñas (≥ 8, mayúscula, minúscula y número) | `UsuarioService.validarContrasena` |
| RNF-S7 | Seguridad | Registro de eventos de seguridad (login, bloqueo, acceso denegado) | Logger `SEGURIDAD`, triggers `users` |
| RNF-S8 | Seguridad | Usuario de BD con mínimo privilegio y auditoría inmutable | `database/perfumeriayvi_v2.sql` |
| RNF-F1 | Fiabilidad | Ventas atómicas: todo o nada | `@Transactional` en `VentaService` |
| RNF-F2 | Fiabilidad | Validación en servidor con mensajes claros, sin páginas de error técnicas | Servicios + `ReglaNegocioException` + ViewModels |
| RNF-F3 | Fiabilidad | Integridad referencial: no eliminar registros con dependencias | FK en BD + mensajes en ViewModels |
| RNF-U1 | Usabilidad | Interfaz web adaptable (Bootstrap 5) | Vistas JSP |
| RNF-U2 | Usabilidad | Confirmación antes de eliminar y mensajes de éxito o error | Vistas JSP + ViewModels |
| RNF-U3 | Usabilidad | Los formularios conservan los datos cuando hay un error | ViewModels (`vm.cliente`, `vm.proveedor`, `vm.producto`) |
| RNF-M1 | Mantenibilidad | Arquitectura MVVM en capas (Model, ViewModel, View) | Paquetes `model`, `viewmodel`, `view` |
| RNF-P1 | Portabilidad | Navegadores modernos y MySQL 8 / MariaDB 10.4+ | Vistas estándar HTML5, SQL compatible |

## Verificación de RNF en el monitoreo (etapa MONITOR de DevOps)

DevOps no es un requisito del IEEE 830, pero el tablero de Grafana permite comprobar en vivo varios RNF.
Detalle en [MONITOREO.md](MONITOREO.md).

| RNF | Panel / alerta | Métrica |
|---|---|---|
| RNF-R1 | "Respuestas ≤ 2 s" y "Tiempo de respuesta p95" · alerta `RespuestaLenta` | `http_server_requests_seconds_bucket` |
| RNF-R3 | "Conexiones a la base de datos" · alerta `PoolConexionesSaturado` | `hikaricp_connections_*` |
| RNF-S2 | "Cuentas bloqueadas" · alerta `CuentaBloqueada` | `perfumeria_cuentas_bloqueadas_total` |
| RNF-S4 | "Formularios sin CSRF" · alerta `PeticionesSinTokenCsrf` | `perfumeria_csrf_rechazados_total` |
| RNF-S7 | "Inicios de sesión", "Accesos denegados", "Bitácora de seguridad en vivo" | `perfumeria_login_total`, Loki `{logger="SEGURIDAD"}` |

## Microservicios

Programas independientes que apoyan requisitos funcionales existentes. Se despliegan como software (`java -jar`), sin Docker. Detalle en [microservicios/README.md](../microservicios/README.md).

| Microservicio | Apoya | HU | Endpoint | Validador | Microservicio (lógica) |
|---|---|---|---|---|---|
| MS-01 Inventario (8091) | RF06, CU06 | HU05 | `ReabastecimientoEndpoint` | `ReabastecimientoValidador` | `ReabastecimientoMicroservicio` |
| MS-02 Facturación (8092) | RF07 (total automático), RF04 (documento del cliente) | HU06 | `FacturaEndpoint` | `FacturaValidador` | `FacturaMicroservicio` |
