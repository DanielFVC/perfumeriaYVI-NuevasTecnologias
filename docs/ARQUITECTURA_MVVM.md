# Arquitectura MVVM – Perfumería YVI

## ¿Por qué MVVM y no MVP o MVW?

| Patrón | Cómo se comunica la vista | ¿Encaja con JSP + Spring Boot? |
|---|---|---|
| **MVP** | El Presenter llama métodos de una interfaz de la vista (`vista.mostrarClientes(lista)`). | No del todo: una JSP no es un objeto con métodos al que se le pueda hablar; se genera una vez por petición. |
| **MVW** | “Model-View-Whatever”: deja libre la pieza intermedia. | Es un nombre genérico, no da reglas claras para organizar el proyecto. |
| **MVVM** | La vista se **enlaza** (data binding) a propiedades del ViewModel y dispara sus comandos. | **Sí.** Las JSP ya se enlazan de forma declarativa con expresiones `${vm.clientes}`, `${vm.error}`. |

Por eso se eligió **MVVM**: el ViewModel expone el estado de la pantalla y la JSP lo muestra sin lógica propia.

## Capas

```
 Navegador
    │  HTTP (GET / POST + token CSRF)
    ▼
┌──────────────────────── VIEW ────────────────────────┐
│ view.binder.*Binder   →  recibe la petición, llama el │
│                          comando del ViewModel y      │
│                          elige la JSP                 │
│ WEB-INF/views/*.jsp   →  se enlaza a ${vm.propiedad}  │
└───────────────────────────┬──────────────────────────┘
                            │ comandos: registrar(), eliminar()...
                            ▼
┌────────────────────── VIEWMODEL ─────────────────────┐
│ viewmodel.*ViewModel  →  estado de la pantalla        │
│   (listas, formulario, mensaje, error) y comandos.    │
│   No conoce HTTP ni JSP → se prueba con JUnit.        │
└───────────────────────────┬──────────────────────────┘
                            │
                            ▼
┌──────────────────────── MODEL ───────────────────────┐
│ model.service    →  reglas de negocio y validaciones  │
│ model.repository →  acceso a MySQL (JdbcTemplate)     │
│ model.entity     →  datos                             │
└──────────────────────────────────────────────────────┘

Transversal: seguridad.* (autenticación, autorización, CSRF, cabeceras)
```

## Ejemplo: registrar un cliente

1. La JSP `registrar_cliente.jsp` envía el formulario a `POST /clientes/registrar`.
2. `ClienteBinder.registrar()` crea un `ClienteViewModel` y ejecuta `vm.registrar(datos, usuario)`.
3. El ViewModel llama a `ClienteService.registrar()` (Model), que valida y guarda.
4. Si hay error, el ViewModel guarda `error` y conserva el formulario (`vm.cliente`); la JSP lo muestra.
   Si todo sale bien, el binder redirige al listado con el mensaje de éxito (patrón POST-Redirect-GET).

## Antes (MVC) y ahora (MVVM)

| Antes | Ahora |
|---|---|
| `controller/*Controller` con lógica y `model.addAttribute("clientes", ...)` | `view/binder/*Binder` solo conecta; el estado vive en `viewmodel/*ViewModel` |
| JSP con variables sueltas (`${clientes}`, `${error}`) | JSP enlazadas a un solo objeto `${vm...}` |
| Validaciones con `"OK"` o excepciones genéricas (error 500) | `ReglaNegocioException` → mensaje claro en `vm.error` |
| `AuthFilter` con `@WebFilter` (no funcionaba con Spring Boot embebido) | `AutorizacionInterceptor` + `@RequiereAcceso` por módulo |
| Eliminar con enlaces GET | Formularios POST con token CSRF |
| Exportar auditoría lanzaba `UnsupportedOperationException` | `ExportadorAuditoria` genera Excel y PDF |
| La venta no descontaba inventario | La venta valida y descuenta stock en una transacción |
