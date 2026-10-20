# Demostración de BUILD, TEST, RELEASE, DEPLOY y OPERATE

Todos los comandos se escriben en **CMD (Símbolo del sistema)**, desde la carpeta del proyecto.
La etapa MONITOR tiene su propia guía: [MONITOREO.md](MONITOREO.md).

## 0. Preparación (antes de empezar)

1. Abra **Docker Desktop** y espere a que diga **Engine running**.
2. Abra CMD y entre a la carpeta del proyecto:
   ```
   cd "C:\Users\ASUS\OneDrive\Desktop\Documentos\DANIEL VILLALBA\5 SEMESTRE\PROGRAMACION JAVA\com.mycompany_perfumeriaYVI_war_1.0-SNAPSHOT"
   ```
3. Active las tildes en la consola y prepare Java 21 y Maven (el que trae NetBeans):
   ```
   chcp 65001
   call devops\entorno.bat
   set COMPOSE_PROFILES=monitoreo
   ```
   Debe mostrar `version "21..."` y `Apache Maven 3.9...`. La última línea hace que los comandos de Docker Compose incluyan siempre los contenedores de monitoreo.

> Ensaye la ruta completa una vez antes de exponer. La primera vez Maven descarga librerías y tarda más.

---

## 1. BUILD: convertir el código en ejecutables

| # | Comando | Qué se muestra |
|---|---|---|
| 1.1 | `mvn -B clean package -DskipTests` | Maven compila la aplicación web y termina en **BUILD SUCCESS** |
| 1.2 | `dir target\perfumeriaYVI.war` | El ejecutable de la aplicación web |
| 1.3 | `mvn -B -f microservicios\pom.xml clean package -DskipTests` | Compila los 2 microservicios: tres líneas **SUCCESS** en el *Reactor Summary* |
| 1.4 | `dir microservicios\ms-inventario\target\*.jar microservicios\ms-facturacion\target\*.jar` | `ms-inventario.jar` y `ms-facturacion.jar` |
| 1.5 | `docker compose build app` | Docker construye la imagen en **2 etapas**: compila con Maven y empaqueta solo con Java 21 |
| 1.6 | `docker images perfumeria-yvi` | La imagen `perfumeria-yvi:local` creada |

**En GitHub:** pestaña **Actions** → último run → job **Build y pruebas**. Abajo, en *Artifacts*, están `perfumeriaYVI-war` y `microservicios-jar`. Así se ve que GitHub compila solo en cada push.

---

## 2. TEST: comprobar que todo funciona antes de liberar

| # | Comando | Qué se muestra |
|---|---|---|
| 2.1 | `mvn -B verify` | **Tests run: 54, Failures: 0, Errors: 0** (Model, seguridad y ViewModels) |
| 2.2 | `start target\site\jacoco\index.html` | Reporte de **cobertura** de la aplicación en el navegador |
| 2.3 | `mvn -B -f microservicios\pom.xml verify` | **18** pruebas de ms-inventario y **17** de ms-facturacion: endpoint, validador y lógica |
| 2.4 | `start microservicios\ms-inventario\target\site\jacoco\index.html` | Cobertura del microservicio (≈ 98 %) |
| 2.5 | `start microservicios\ms-facturacion\target\site\jacoco\index.html` | Cobertura del microservicio (≈ 96 %) |

**En GitHub:** el job **Build y pruebas** corre las mismas 89 pruebas. Si una falla, el pipeline se pone en rojo y **no se libera nada**. El artifact `reportes-pruebas` guarda los resultados.

*(La prueba de carga con k6 también es TEST, pero se muestra en la parte de monitoreo.)*

---

## 3. RELEASE: publicar una versión identificable

**Antes de la exposición** (una sola vez):
1. En **GitHub Desktop**, rama `develop`: **Commit** de los cambios → **Push origin**. Espere el check verde en Actions.
2. Cámbiese a `main` → **Branch → Merge into current branch → develop** → **Push origin**.
3. Cree la primera versión:
   ```
   git checkout main
   git pull
   git tag -a v2.1.0 -m "Perfumeria YVI v2.1.0: microservicios de inventario y facturacion"
   git push origin v2.1.0
   ```
   Otra forma: en GitHub Desktop, pestaña **History** → clic derecho en el último commit → **Create Tag…** → `v2.1.0` → **Push origin**.

**En la exposición:**

| # | Dónde | Qué se muestra |
|---|---|---|
| 3.1 | CMD: `git tag` | Las versiones creadas (`v2.1.0`) |
| 3.2 | CMD: `git log --oneline --decorate -5` | El commit exacto al que apunta la etiqueta |
| 3.3 | GitHub → **Actions** | El run de `v2.1.0` con los jobs **Build y pruebas → Imagen Docker → Publicar versión** |
| 3.4 | GitHub → **Releases** → `v2.1.0` | La versión con sus 3 archivos: `perfumeriaYVI.war`, `ms-inventario.jar` y `ms-facturacion.jar` |
| 3.5 | GitHub → **Packages** → `perfumeria-yvi` | La imagen Docker con las etiquetas `v2.1.0`, `latest` y la del commit |

Si quiere mostrar una versión creándose en vivo, use una nueva etiqueta (por ejemplo `v2.1.1`) con los mismos dos comandos `git tag` y `git push`. El pipeline tarda unos 4 minutos.

---

## 4. DEPLOY: poner el sistema a funcionar

### 4.1 Aplicación web, base de datos y monitoreo (con Docker)
```
docker compose --profile monitoreo up -d
docker compose ps
```
Todos deben quedar en **Up**, y la app en **(healthy)**. Abra http://localhost:8080 (admin / Admin2026*).

### 4.2 Microservicios (como software, sin Docker)
```
microservicios\desplegar.bat
```
El script detiene la versión anterior, copia los JAR a `%USERPROFILE%\PerfumeriaYVI-servidor`, abre **una ventana por servicio** y verifica la salud. Al final debe mostrar dos líneas **[ARRIBA]**.

Si el repositorio es público, también se puede desplegar **la versión publicada en el RELEASE**:
```
microservicios\desplegar.bat release
```

> Si el vigilante (paso 5.6) está abierto, ciérrelo antes de volver a desplegar.

### 4.3 Probar que funcionan

| # | Comando | Resultado |
|---|---|---|
| a | `start http://localhost:8091/api/v1/info` | Qué hace el microservicio de inventario |
| b | `curl -X POST http://localhost:8091/api/v1/reabastecimiento -H "Content-Type: application/json" --data-binary @microservicios\ms-inventario\ejemplos\producto-stock-bajo.json -w "\n"` | `"nivel":"CRITICO"`, `"cantidadSugerida":67` |
| c | `curl -X POST http://localhost:8091/api/v1/reabastecimiento -H "Content-Type: application/json" --data-binary @microservicios\ms-inventario\ejemplos\producto-invalido.json -w "\n"` | `"estado":400` con la lista de errores del **validador** |
| d | `curl -X POST http://localhost:8092/api/v1/facturas -H "Content-Type: application/json" --data-binary @microservicios\ms-facturacion\ejemplos\factura.json -w "\n"` | subtotal 142000, IVA 24282, `"total":152082` |
| e | `curl -X POST http://localhost:8092/api/v1/facturas -H "Content-Type: application/json" --data-binary @microservicios\ms-facturacion\ejemplos\factura-invalida.json -w "\n"` | `"estado":400`: NIT con dígito de verificación equivocado, descuento de 70 %, producto repetido… |
| f | `start "" "http://localhost:8092/api/v1/documentos/validar?tipo=NIT&numero=890903938-8"` | `"valido":true` en el navegador |

**En GitHub:** job **Despliegue a producción**, que usa el entorno `produccion` con aprobación manual. Si aparece *Waiting*, pulse **Review deployments → Approve and deploy**.

---

## 5. OPERATE: mantenerlo funcionando

| # | Comando | Qué se muestra |
|---|---|---|
| 5.1 | `microservicios\estado.bat` | Salud de ms-inventario, ms-facturacion y la aplicación web |
| 5.2 | `docker compose ps` | Contenedores **Up (healthy)**: Docker revisa la salud cada 30 s |
| 5.3 | `docker stats --no-stream` | CPU y memoria que usa cada contenedor |
| 5.4 | `docker compose exec app pkill java` | **Simula una caída** de la aplicación web |
| 5.5 | `docker compose ps` (repetir a los 20–60 s) | La app vuelve sola: *health: starting* → **healthy** (`restart: unless-stopped`) |
| 5.6 | `start microservicios\vigilante.bat` | Abre el **vigilante** de los microservicios en otra ventana |
| 5.7 | Cierre con la **X** la ventana `ms-inventario (8091)` | **Simula una caída**: antes de 10 s el vigilante la detecta y la vuelve a arrancar |
| 5.8 | `type "%USERPROFILE%\PerfumeriaYVI-servidor\vigilante.log"` | Registro del incidente con fecha y hora |
| 5.9 | `findstr REABASTECIMIENTO "%USERPROFILE%\PerfumeriaYVI-servidor\ms-inventario\logs\ms-inventario.log"` | Los logs del microservicio: cada cálculo que hizo |
| 5.10 | `mkdir "%USERPROFILE%\PerfumeriaYVI-servidor\respaldos"` | Carpeta para los respaldos (si ya existe, el aviso no importa) |
| 5.11 | `docker compose exec -T db sh -c "mysqldump -uroot -p\"$MARIADB_ROOT_PASSWORD\" --databases perfumeriayvi_v2" > "%USERPROFILE%\PerfumeriaYVI-servidor\respaldos\respaldo.sql"` | **Respaldo** de la base de datos |
| 5.12 | `findstr /c:"CREATE TABLE" "%USERPROFILE%\PerfumeriaYVI-servidor\respaldos\respaldo.sql"` | Las tablas que quedaron en el respaldo |

Con Prometheus encendido, el paso 5.4 también dispara la alerta **AplicacionCaida** en http://localhost:9093. Ahí se conecta OPERATE con MONITOR.

---

## 6. Al terminar

Primero cierre la ventana del vigilante con **Ctrl + C** (responda `S`) o con la X; si sigue abierto, vuelve a arrancar los servicios. Después:
```
microservicios\detener.bat
docker compose --profile monitoreo down
```
