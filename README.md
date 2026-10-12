# Perfumería YVI – Sistema de inventario y ventas (v2.0, MVVM)

Aplicación web en **Spring Boot 3.5 + JSP + MySQL/MariaDB** para gestionar clientes, proveedores,
productos, ventas, vendedores y auditoría de la Perfumería YVI.

- **Arquitectura:** MVVM (Model – ViewModel – View). Ver [docs/ARQUITECTURA_MVVM.md](docs/ARQUITECTURA_MVVM.md).
- **Seguridad:** autenticación con BCrypt, bloqueo por intentos fallidos, autorización por rol y permisos,
  protección CSRF, sesión segura, cabeceras HTTP y escape de salida contra XSS.
- **DevOps:** pruebas automáticas, Docker, Docker Compose y pipeline CI/CD. Ver [docs/DEVOPS.md](docs/DEVOPS.md).
- **Monitoreo:** Prometheus, Grafana, Alertmanager y Loki con un tablero que mide los requisitos no funcionales
  en vivo. Guía de la demostración en [docs/MONITOREO.md](docs/MONITOREO.md).
- **Trazabilidad:** cada clase indica el requisito que implementa (RF, RNF, CU, HU).
  Índice en [docs/TRAZABILIDAD.md](docs/TRAZABILIDAD.md).

## Estructura

```
src/main/java/com/mycompany/perfumeriayvi/
├── PerfumeriaApplication.java     Punto de entrada (WAR para Tomcat o java -jar)
├── config/                        Configuración web y reloj
├── seguridad/                     Autenticación, autorización, CSRF, cabeceras, bitácora
├── monitoreo/                     Métricas propias para Prometheus/Grafana
├── model/                         MODEL
│   ├── entity/                      Cliente, Proveedor, Producto, Venta, DetalleVenta, Usuario, Auditoria
│   ├── repository/                  Interfaces + implementaciones JDBC (JdbcTemplate)
│   └── service/                     Reglas de negocio y validaciones
├── viewmodel/                     VIEWMODEL: estado y comandos de cada pantalla
├── view/binder/                   VIEW: conecta la petición HTTP con el ViewModel
└── util/HashGenerator.java        Genera hashes BCrypt desde consola
src/main/webapp/WEB-INF/views/     VIEW: páginas JSP enlazadas a ${vm.propiedad}
src/test/java/                     Pruebas unitarias (Model, seguridad y ViewModels)
database/perfumeriayvi_v2.sql      Base de datos ficticia, robusta y segura
docker/, Dockerfile, docker-compose.yml, devops/ci-cd.yml      DevOps (el pipeline va en .github/workflows/)
monitoreo/                         Prometheus, alertas, Grafana, Loki/Promtail y scripts k6 de la demo
```

## Ejecutar en NetBeans + XAMPP

1. Encienda **Apache** y **MySQL** en XAMPP.
2. En phpMyAdmin: **Importar** → `database/perfumeriayvi_v2.sql` → **Continuar**.
   Se crea la base `perfumeriayvi_v2` y el usuario de conexión `perfumeria_app`
   (su base original `perfumeriayvi` no se modifica).
3. En NetBeans: clic derecho al proyecto → **Clean and Build** → **Run** (Tomcat).
4. Abra `http://localhost:8080/perfumeriaYVI/login` (o la ruta que muestre NetBeans).

Usuarios de prueba (contraseñas ficticias):

| Usuario | Contraseña | Rol / permisos |
|---|---|---|
| admin | Admin2026* | ADMIN – todo |
| ana.martinez | Vendedor2026* | VENDEDOR – todos los módulos |
| carlos.rios | Vendedor2026* | VENDEDOR – clientes y ventas |
| sofia.leon | Vendedor2026* | VENDEDOR – productos y proveedores |
| pedro.gil | Vendedor2026* | VENDEDOR – inactivo (no puede entrar) |

Para conectarse a otra base o con otro usuario, defina las variables de entorno
`DB_URL`, `DB_USER` y `DB_PASSWORD` (ver `application.properties`).

## Ejecutar con Docker

```bash
cp .env.example .env        # cambie las claves
docker compose up -d --build
# http://localhost:8080   ·   salud: docker compose ps (columna STATUS = healthy)
```

### Con monitoreo (Prometheus + Grafana)

```bash
docker compose --profile monitoreo up -d --build
# Grafana http://localhost:3000 (admin / Grafana2026*) · Prometheus :9090 · Alertmanager :9093
docker compose run --rm trafico                          # simula un turno de ventas
docker compose run --rm trafico run /scripts/ataque.js   # simula ataques (bloqueo, CSRF, 403)
```

## Pruebas

```bash
mvn verify                  # pruebas + reporte de cobertura en target/site/jacoco/index.html
```
"# perfumeria-yvi" 
