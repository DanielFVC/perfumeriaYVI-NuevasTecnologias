# DevOps – Perfumería YVI

El ciclo DevOps se implementa con herramientas incluidas en el proyecto.

| Etapa | Qué se hace | Dónde está |
|---|---|---|
| **1. Plan** | Requisitos, historias de usuario y casos de uso con códigos (RF, RNF, CU, HU); tablero de tareas (GitHub Projects o Trello). | Documento IEEE 830, `docs/TRAZABILIDAD.md` |
| **2. Code** | Git con ramas: `main` (producción), `develop` (integración), `feature/<nombre>` (cada cambio). Todo cambio entra por Pull Request revisado. Cada clase cita su requisito. | Repositorio Git, `.gitignore` |
| **3. Build** | Maven compila y empaqueta el WAR de la aplicación y los JAR de los 2 microservicios; Docker construye la imagen en dos etapas. En GitHub Actions ocurre solo en cada push. | `pom.xml`, `microservicios/pom.xml`, `Dockerfile`, `microservicios/construir.bat` |
| **4. Test** | Pruebas unitarias automáticas: 54 de la aplicación (Model, seguridad y ViewModels) y 35 de los microservicios (endpoint, validador y lógica), con reporte de cobertura JaCoCo. Pruebas de carga con k6. El pipeline falla si una prueba falla. | `src/test/java`, `microservicios/*/src/test/java`, `mvn verify` |
| **5. Release** | Al integrar en `main` se publica la imagen `ghcr.io/<usuario>/perfumeria-yvi:latest`. Al crear una etiqueta `v*` (ej. `v2.1.0`) se publica la imagen con esa versión y una **GitHub Release** con el WAR y los JAR de los microservicios. | `devops/ci-cd.yml` → `.github/workflows/` (jobs `docker` y `release`) |
| **6. Deploy** | Aplicación: Docker Compose (app + MariaDB + monitoreo). Microservicios: como software con `java -jar` en `%USERPROFILE%\PerfumeriaYVI-servidor` (`desplegar.bat`, o `desplegar.bat release` para bajar la versión publicada). En producción requiere aprobación manual del entorno `produccion`. | `docker-compose.yml`, `microservicios/desplegar.bat`, job `deploy` |
| **7. Operate** | Configuración por variables de entorno y perfiles (`dev`, `prod`); contenedor sin root; HEALTHCHECK y reinicio automático en Docker; `estado.bat` y `vigilante.bat` (reinicio automático) para los microservicios; respaldo de la base de datos; **Prometheus** vigila la app cada 5 s y **Alertmanager** gestiona los incidentes (app caída, respuesta lenta, cuentas bloqueadas, stock bajo). | `application-*.properties`, `.env.example`, `microservicios/estado.bat`, `microservicios/vigilante.bat`, `monitoreo/prometheus/alertas.yml` |
| **8. Monitor** | Métricas con Actuator + Micrometer (`/actuator/prometheus`), tablero **Grafana** con los RNF medibles en vivo, logs centralizados con **Loki + Promtail** (misma función que ELK), HEALTHCHECK de Docker, bitácora `SEGURIDAD` y auditoría en BD. Guía de la demostración: [MONITOREO.md](MONITOREO.md). | `monitoreo/`, `MetricasPerfumeria`, `docker-compose.yml` (perfil `monitoreo`) |

## Pipeline CI/CD

```
push / pull request / etiqueta v*
      │
      ▼
 build-test ──► mvn verify (app) + mvn -f microservicios/pom.xml verify ──► artefactos: WAR, JAR y reportes
      │
      ├──► docker   (rama main o etiqueta v*) ──► imagen en GitHub Container Registry (latest, commit y versión)
      │       │
      │       ▼
      │    deploy   ──► entorno "produccion" (aprobación manual) ──► docker compose pull/up + verificación de salud
      │
      └──► release  (solo etiqueta v*) ──► GitHub Release con perfumeriaYVI.war, ms-inventario.jar y ms-facturacion.jar
```

### Activarlo en GitHub
1. Copie `devops/ci-cd.yml` a la carpeta `.github/workflows/` del proyecto (créela si no existe)
   y suba el proyecto a un repositorio de GitHub.
2. En **Settings → Environments** cree `produccion` con revisores obligatorios.
3. (Opcional) En **Settings → Secrets** agregue `SERVIDOR_HOST`, `SERVIDOR_USUARIO` y `SERVIDOR_CLAVE_SSH`
   para desplegar en un servidor propio por SSH.

## Perfiles

| Perfil | Uso | Diferencias |
|---|---|---|
| `dev` (por defecto) | NetBeans + XAMPP | Logs DEBUG, salud detallada |
| `prod` | Docker / servidor | Logs en archivo con rotación, cookie de sesión solo por HTTPS, sin detalles técnicos |

Variables de entorno principales: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SPRING_PROFILES_ACTIVE`, `PORT`, `COOKIE_SECURE`, `MANAGEMENT_PORT` (puerto interno de salud y métricas, 8081 en Docker), `GRAFANA_PASSWORD`.
