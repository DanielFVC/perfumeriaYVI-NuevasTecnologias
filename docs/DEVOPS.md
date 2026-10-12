# DevOps – Perfumería YVI

El ciclo DevOps se implementa con herramientas incluidas en el proyecto.

| Etapa | Qué se hace | Dónde está |
|---|---|---|
| **1. Plan** | Requisitos, historias de usuario y casos de uso con códigos (RF, RNF, CU, HU); tablero de tareas (GitHub Projects o Trello). | Documento IEEE 830, `docs/TRAZABILIDAD.md` |
| **2. Code** | Git con ramas: `main` (producción), `develop` (integración), `feature/<nombre>` (cada cambio). Todo cambio entra por Pull Request revisado. Cada clase cita su requisito. | Repositorio Git, `.gitignore` |
| **3. Build** | Maven compila y empaqueta el WAR; Docker construye la imagen en dos etapas. | `pom.xml`, `Dockerfile` |
| **4. Test** | Pruebas unitarias automáticas (Model, seguridad y ViewModels) y reporte de cobertura JaCoCo. El pipeline falla si una prueba falla. | `src/test/java`, `mvn verify` |
| **5. Release** | Al integrar en `main`, el pipeline publica el WAR y la imagen `ghcr.io/<usuario>/perfumeria-yvi` con la etiqueta del commit. | `devops/ci-cd.yml` → `.github/workflows/` (job `docker`) |
| **6. Deploy** | Despliegue con Docker Compose (app + MariaDB). En producción requiere aprobación manual del entorno `produccion`. | `docker-compose.yml`, job `deploy` |
| **7. Operate** | Configuración por variables de entorno y perfiles (`dev`, `prod`); contenedor sin root; reinicio automático; **Prometheus** vigila la app cada 5 s y **Alertmanager** gestiona los incidentes (app caída, respuesta lenta, cuentas bloqueadas, stock bajo). | `application-*.properties`, `.env.example`, `monitoreo/prometheus/alertas.yml`, `monitoreo/alertmanager/` |
| **8. Monitor** | Métricas con Actuator + Micrometer (`/actuator/prometheus`), tablero **Grafana** con los RNF medibles en vivo, logs centralizados con **Loki + Promtail** (misma función que ELK), HEALTHCHECK de Docker, bitácora `SEGURIDAD` y auditoría en BD. Guía de la demostración: [MONITOREO.md](MONITOREO.md). | `monitoreo/`, `MetricasPerfumeria`, `docker-compose.yml` (perfil `monitoreo`) |

## Pipeline CI/CD

```
push / pull request
      │
      ▼
 build-test ──► mvn verify (compila + pruebas + JaCoCo) ──► artefactos: WAR y reportes
      │  (solo rama main)
      ▼
   docker   ──► construye y publica la imagen en GitHub Container Registry
      │
      ▼
   deploy   ──► entorno "produccion" (aprobación manual) ──► docker compose pull/up + verificación de salud
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
