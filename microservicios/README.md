# Microservicios – Perfumería YVI

Son dos programas independientes de la aplicación web. Cada uno tiene su propio proceso, su propio puerto y sus propias pruebas. Se despliegan **como software**, con `java -jar` y **sin Docker**.

| | MS-01 Inventario | MS-02 Facturación |
|---|---|---|
| **Qué hace** | Dice si un producto necesita reabastecerse y cuántas unidades pedir al proveedor | Calcula subtotal, descuento, IVA (19 %) y total de una venta |
| **Requisito que apoya** | RF06 (alertas de stock bajo) | RF07 y RF08 (ventas) |
| **Puerto** | 8091 | 8092 |
| **Ejecutable** | `ms-inventario.jar` | `ms-facturacion.jar` |
| **Pruebas** | 18 (98 % de cobertura) | 17 (96 % de cobertura) |

## Dónde está cada archivo

### MS-01 Inventario: `microservicios/ms-inventario/`

| Pieza | Archivo |
|---|---|
| **Endpoint** | `src/main/java/com/perfumeriayvi/inventario/endpoint/ReabastecimientoEndpoint.java` |
| **Validador** | `src/main/java/com/perfumeriayvi/inventario/validador/ReabastecimientoValidador.java` |
| **Microservicio** (lógica) | `src/main/java/com/perfumeriayvi/inventario/microservicio/ReabastecimientoMicroservicio.java` |
| Arranque (`main`) | `src/main/java/com/perfumeriayvi/inventario/MsInventarioAplicacion.java` |
| Datos de entrada y salida | `src/main/java/com/perfumeriayvi/inventario/modelo/` |
| Error de validación | `src/main/java/com/perfumeriayvi/inventario/validador/ValidacionException.java` |
| Configuración (puerto, logs) | `src/main/resources/application.properties` |
| Pruebas | `src/test/java/com/perfumeriayvi/inventario/` (una por cada pieza) |
| JSON de ejemplo | `ejemplos/` |

### MS-02 Facturación: `microservicios/ms-facturacion/`

| Pieza | Archivo |
|---|---|
| **Endpoint** | `src/main/java/com/perfumeriayvi/facturacion/endpoint/FacturaEndpoint.java` |
| **Validador** | `src/main/java/com/perfumeriayvi/facturacion/validador/FacturaValidador.java` |
| **Microservicio** (lógica) | `src/main/java/com/perfumeriayvi/facturacion/microservicio/FacturaMicroservicio.java` |
| Arranque (`main`) | `src/main/java/com/perfumeriayvi/facturacion/MsFacturacionAplicacion.java` |
| Datos de entrada y salida | `src/main/java/com/perfumeriayvi/facturacion/modelo/` |
| Error de validación | `src/main/java/com/perfumeriayvi/facturacion/validador/ValidacionException.java` |
| Configuración (puerto, logs) | `src/main/resources/application.properties` |
| Pruebas | `src/test/java/com/perfumeriayvi/facturacion/` (una por cada pieza) |
| JSON de ejemplo | `ejemplos/` |

### Scripts de DevOps: `microservicios/`

| Script | Etapa | Qué hace |
|---|---|---|
| `construir.bat` | BUILD + TEST | Compila, corre las pruebas, genera la cobertura y crea los JAR |
| `desplegar.bat` | DEPLOY | Copia los JAR a `%USERPROFILE%\PerfumeriaYVI-servidor` y los arranca. Con `release`, los descarga de GitHub Releases |
| `estado.bat` | OPERATE | Revisa la salud de los dos servicios y de la aplicación web |
| `vigilante.bat` | OPERATE | Si un servicio se cae, lo vuelve a arrancar solo |
| `detener.bat` | OPERATE | Detiene los dos servicios |
| `../devops/entorno.bat` | – | Prepara Java 21 y Maven (el de NetBeans) en la ventana de CMD |

## Cómo funciona una petición

```
Cliente (navegador, curl o la app)
        │  HTTP + JSON
        ▼
  ENDPOINT ──► VALIDADOR ──¿datos correctos?── no ──► 400 + lista de errores
                               │ sí
                               ▼
                         MICROSERVICIO (cálculo) ──► 200 + resultado en JSON
```

## Endpoints

**MS-01 Inventario (http://localhost:8091)**
- `POST /api/v1/reabastecimiento`: cuerpo JSON (ver `ejemplos/producto-stock-bajo.json`)
- `GET /api/v1/reabastecimiento?idProducto=1&stockActual=4&stockMinimo=5&ventasDiarias=2&diasEntregaProveedor=3`
- `GET /api/v1/info`
- `GET /actuator/health`

**MS-02 Facturación (http://localhost:8092)**
- `POST /api/v1/facturas`: cuerpo JSON (ver `ejemplos/factura.json`)
- `GET /api/v1/documentos/validar?tipo=NIT&numero=890903938-8`
- `GET /api/v1/info`
- `GET /actuator/health`

## Uso rápido (CMD, desde la carpeta del proyecto)

```
microservicios\construir.bat
microservicios\desplegar.bat
curl -X POST http://localhost:8091/api/v1/reabastecimiento -H "Content-Type: application/json" --data-binary @microservicios\ms-inventario\ejemplos\producto-stock-bajo.json
curl -X POST http://localhost:8092/api/v1/facturas -H "Content-Type: application/json" --data-binary @microservicios\ms-facturacion\ejemplos\factura.json
microservicios\detener.bat
```

En NetBeans, cada microservicio se abre con **File → Open Project → microservicios → ms-inventario** (o ms-facturacion). Para ejecutarlo se usa **Run** (F6).
