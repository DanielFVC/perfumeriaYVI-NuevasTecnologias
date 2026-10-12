# Monitoreo – Perfumería YVI (etapas 7 OPERAR y 8 MONITOREAR)

Esta guía explica qué se monitorea, cómo levantarlo y el guion de la demostración.

## 1. Qué herramienta cumple cada función

| Función del ciclo de vida | Herramienta de la presentación | Lo que usamos | Dónde se ve |
|---|---|---|---|
| Exponer métricas de la app | APM (New Relic / Dynatrace) | **Spring Boot Actuator + Micrometer** | `/actuator/prometheus` (puerto interno 8081) |
| Monitorizar la salud del sistema | Nagios / **Prometheus** | **Prometheus** (lee la app cada 5 s) | http://localhost:9090 |
| Gestión de incidentes | ServiceNow / PagerDuty | **Alertmanager** (agrupa y avisa) | http://localhost:9093 |
| Tableros de rendimiento | New Relic / Dynatrace | **Grafana** | http://localhost:3000 |
| Gestión de registros (logs) | **ELK** (Elasticsearch, Logstash, Kibana) | **Loki + Promtail** (misma función, usa mucha menos memoria) | Grafana → panel "Bitácora de seguridad" |
| Prueba de carga para la demo | – | **k6** | `docker compose run --rm trafico` |

> Equivalencias para explicar en la exposición: Promtail = Logstash (recoge logs), Loki = Elasticsearch (los guarda),
> Grafana = Kibana (los muestra).

## 2. Qué se mide y qué requisito comprueba

| Panel de Grafana | Métrica de Prometheus | Requisito que comprueba |
|---|---|---|
| Aplicación EN LÍNEA / CAÍDA | `up{job="perfumeria-app"}` | Disponibilidad (alerta `AplicacionCaida`) |
| Respuestas ≤ 2 s (meta 95 %) y tiempo p95 | `http_server_requests_seconds_bucket` | **RNF-R1** (alerta `RespuestaLenta`) |
| Conexiones a la base de datos | `hikaricp_connections_*` | **RNF-R3** máx. 10 (alerta `PoolConexionesSaturado`) |
| Sesiones activas | `tomcat_sessions_active_current_sessions` | RNF-S3 |
| Inicios de sesión exitosos / fallidos | `perfumeria_login_total{resultado}` | RF01, **RNF-S7** |
| Cuentas bloqueadas | `perfumeria_cuentas_bloqueadas_total` | **RNF-S2** (alerta `CuentaBloqueada`) |
| Formularios sin CSRF | `perfumeria_csrf_rechazados_total` | **RNF-S4** (alerta `PeticionesSinTokenCsrf`) |
| Accesos denegados | `perfumeria_accesos_denegados_total` | RF03, RNF-S7 |
| Ventas por minuto y monto vendido | `perfumeria_ventas_total{operacion}`, `perfumeria_ventas_monto_pesos_sum` | RF07, RF08 |
| Productos con stock bajo | `perfumeria_productos_stock_bajo` | RF06 (alerta `StockBajoEnAumento`) |
| Bitácora de seguridad en vivo | Loki `{logger="SEGURIDAD"}` | **RNF-S7** |
| Memoria, CPU, tiempo encendida | `jvm_memory_*`, `process_*` | Operación |

Las métricas propias están en `src/main/java/com/mycompany/perfumeriayvi/monitoreo/`.

## 3. Requisitos del computador

- **Docker Desktop** para Windows (con WSL 2 activado). Verifique con `docker --version`.
- Unos **2 GB de RAM libres** para todo el entorno (app, MariaDB, Prometheus, Grafana, Loki).
- Internet **solo la primera vez** (descarga imágenes y dependencias, 10–15 minutos).
  Hágalo antes del día de la exposición; después funciona sin internet.

## 4. Levantar todo

Abra una terminal (PowerShell) en la carpeta del proyecto:

```powershell
copy .env.example .env                              # solo la primera vez
docker compose --profile monitoreo up -d --build    # app + BD + monitoreo
docker compose ps                                   # espere a que "perfumeria-app" diga (healthy)
```

| Qué | Dirección | Usuario |
|---|---|---|
| Aplicación | http://localhost:8080 | admin / Admin2026* |
| Grafana (tablero) | http://localhost:3000 | admin / Grafana2026* (o la clave de su `.env`) |
| Prometheus | http://localhost:9090 (menú *Alerts* y *Status → Targets*) | – |
| Alertmanager | http://localhost:9093 | – |

El tablero **"Perfumería YVI · Monitoreo"** abre como página de inicio de Grafana y se actualiza cada 5 s.

Apagar: `docker compose --profile monitoreo down` (conserva los datos) · borrar todo: añada `-v`.

## 5. Guion de la demostración (≈ 6 minutos)

**Antes de empezar (importante si ensayó antes):** cada ensayo deja cuentas bloqueadas y stock vendido en la BD,
y la demo de ataques no volvería a disparar la alerta. Deje todo como nuevo unos 5 minutos antes:

```powershell
docker compose --profile monitoreo down -v
docker compose --profile monitoreo up -d --build
docker compose ps        # espere (healthy) en perfumeria-app
```

Luego abra Grafana en una pestaña y la aplicación en otra. *Alertas activas* debe estar en 0 (verde).

1. **Presentar el tablero (1 min).** "Prometheus lee la aplicación cada 5 segundos; Grafana lo muestra.
   Arriba está el estado general; cada panel dice qué requisito del IEEE 830 comprueba."
2. **Simular un turno de trabajo (2 min).** En la terminal:
   ```powershell
   docker compose run --rm trafico
   ```
   5 usuarios virtuales (ana.martinez, carlos.rios y admin) navegan y registran ventas durante 5 minutos.
   Mostrar cómo suben *Peticiones por segundo*, *Ventas por minuto*, *Conexiones a la BD* y cómo el
   **p95 se mantiene por debajo de la línea roja de 2 s (RNF-R1)**. Los productos más vendidos bajan de stock
   y el panel *Productos con stock bajo* aumenta. Al final k6 imprime `p(95)`, si cumplió la meta (✓),
   y los contadores `ventas_registradas` y `ventas_sin_stock` (ventas rechazadas porque el perfume se agotó, RF07).
3. **Simular ataques (1,5 min).** En otra terminal:
   ```powershell
   docker compose run --rm trafico run /scripts/ataque.js
   ```
   - 6 contraseñas falsas contra `sofia.leon` → *Cuentas bloqueadas* pasa a 1 (**RNF-S2**) y se disparan
     las alertas `CuentaBloqueada` y `PosibleAtaqueDeFuerzaBruta`.
   - Formulario sin token → *Formularios sin CSRF* sube (**RNF-S4**).
   - `carlos.rios` intenta abrir Usuarios, Auditoría y Productos → *Accesos denegados* (**RF03**).
   - En la *Bitácora de seguridad en vivo* aparecen las líneas `LOGIN_FAIL`, `BLOQUEO`, `CSRF_INVALIDO`, `ACCESO_DENEGADO`.
   - *Alertas activas* se pone en rojo: abrir http://localhost:9093 y mostrar las alertas agrupadas.
   - Nota: si el turno del paso 2 vendió mucho, también puede aparecer `StockBajoEnAumento` (es correcto: RF06).
4. **Incidente: la aplicación se cae (1,5 min).**
   ```powershell
   docker compose stop app      # se "cae" la aplicación
   ```
   En ~15 s el panel *Aplicación* pasa a **CAÍDA** y se dispara la alerta crítica `AplicacionCaida`.
   ```powershell
   docker compose start app     # recuperación
   ```
   Cuando vuelve a **EN LÍNEA** la alerta se resuelve sola. Cierre: "Así se cierra el ciclo:
   el monitoreo detecta el problema, Operar lo atiende y lo aprendido vuelve a Planificación."

**Plan B:** grabe la demo en video (Win + G en Windows, o OBS) el día anterior por si falla el equipo.

## 6. Si la aplicación corre en NetBeans (sin Docker para la app)

1. En `monitoreo/prometheus/prometheus.yml` cambie el destino:
   `targets: ["host.docker.internal:8080"]` y `metrics_path: /perfumeriaYVI/actuator/prometheus`.
2. Levante solo el monitoreo, sin la app de Docker:
   `docker compose --profile monitoreo up -d --no-deps prometheus alertmanager grafana`.
3. Tráfico: `docker compose run --rm --no-deps -e BASE_URL=http://host.docker.internal:8080/perfumeriaYVI trafico`.

En este modo los logs no llegan a Loki (el panel de bitácora queda vacío); el resto funciona igual.

## 7. Problemas comunes

| Síntoma | Solución |
|---|---|
| Grafana muestra "No data" | Espere 30 s después de levantar. En Prometheus → *Status → Targets* `perfumeria-app` debe estar **UP**. |
| `perfumeria-app` no queda *healthy* | `docker compose logs app`. Suele ser la BD iniciando: espere y repita `docker compose ps`. |
| k6 dice "inicio de sesión correcto ✗" | La cuenta puede estar bloqueada 15 min por una demo anterior, o la BD no tiene los datos v2. |
| Puerto ocupado (8080, 3000, 9090) | Detenga Tomcat/XAMPP de NetBeans o cambie el puerto izquierdo en `docker-compose.yml`. |
| Quiero empezar de cero | `docker compose --profile monitoreo down -v` y vuelva a levantar. |
