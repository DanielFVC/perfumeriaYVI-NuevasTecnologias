<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - Panel principal. RF03 (CU03): solo se muestran los módulos autorizados (PanelViewModel). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Panel - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
  <style>
    .panel-yvi { max-width: 1140px; }
    .encabezado { padding: 8px 0 28px; border-bottom: 1px solid var(--yvi-linea); margin-bottom: 28px; }
    .encabezado .fecha { color: #8A6420; font-weight: 600; font-size: .85rem; margin-bottom: 4px; }
    .encabezado .saludo { font-family: var(--yvi-serif); font-weight: 600; font-size: clamp(2rem, 4vw, 2.9rem);
                          line-height: 1.1; margin: 0 0 6px; color: var(--yvi-ciruela); }
    .modulo { background: #fff; border: 1px solid var(--yvi-linea); border-radius: 12px; padding: 22px;
              height: 100%; display: flex; flex-direction: column; transition: border-color .15s; }
    .modulo:hover, .modulo:focus-within { border-color: #BFB2D3; }
    .modulo-icono { width: 44px; height: 44px; border-radius: 10px; display: inline-flex; align-items: center;
                    justify-content: center; background: var(--yvi-iris-tenue); color: var(--yvi-iris); font-size: 1.05rem; margin-bottom: 16px; }
    .modulo-nombre { color: var(--yvi-ciruela); font-family: var(--yvi-serif); font-size: 1.35rem; font-weight: 600; margin: 0 0 4px; }
    .modulo-texto { color: var(--yvi-gris); font-size: .88rem; line-height: 1.5; margin: 0 0 20px; flex-grow: 1; }
    .modulo-acciones { display: flex; flex-direction: column; align-items: flex-start; gap: 12px; }
    .modulo-acciones .enlace { color: var(--yvi-iris); font-weight: 600; font-size: .85rem; text-decoration: none; }
    .modulo-acciones .enlace:hover, .modulo-acciones .enlace:focus-visible { text-decoration: underline; text-underline-offset: 3px; }
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container panel-yvi">
  <section class="encabezado d-flex flex-wrap justify-content-between align-items-end gap-3">
    <div>
      <div class="fecha"><c:out value="${vm.fecha}"/></div>
      <h1 class="saludo">Hola, <c:out value="${vm.nombre}"/></h1>
      <p class="text-muted mb-0">${vm.admin ? 'Panel de administración de la perfumería.' : 'Estos son tus módulos de trabajo.'}</p>
    </div>
    <c:if test="${vm.admin}">
      <div class="d-flex flex-wrap gap-2">
        <a href="${ctx}/usuarios" class="btn btn-outline-primary"><i class="fa-solid fa-users-gear me-2"></i>Gestionar vendedores</a>
        <a href="${ctx}/auditoria" class="btn btn-outline-primary"><i class="fa-solid fa-shield-halved me-2"></i>Auditoría</a>
      </div>
    </c:if>
  </section>

  <c:if test="${vm.sinModulos}">
    <div class="alert alert-warning">Aún no tienes módulos habilitados. Pide al administrador que te asigne permisos.</div>
  </c:if>

  <div class="row g-3">
    <c:if test="${vm.puedeVerClientes}">
      <div class="col-12 col-sm-6 col-xl-3">
        <article class="modulo">
          <span class="modulo-icono"><i class="fa-solid fa-user"></i></span>
          <h2 class="modulo-nombre">Clientes</h2>
          <p class="modulo-texto">Registra y consulta los datos de quienes compran en la perfumería.</p>
          <div class="modulo-acciones">
            <a href="${ctx}/clientes/nuevo" class="btn btn-primary btn-sm"><i class="fa-solid fa-plus me-1"></i>Nuevo cliente</a>
            <a href="${ctx}/clientes" class="enlace">Ver clientes</a>
          </div>
        </article>
      </div>
    </c:if>

    <c:if test="${vm.puedeVerProveedores}">
      <div class="col-12 col-sm-6 col-xl-3">
        <article class="modulo">
          <span class="modulo-icono"><i class="fa-solid fa-truck"></i></span>
          <h2 class="modulo-nombre">Proveedores</h2>
          <p class="modulo-texto">Las casas y distribuidores que surten el inventario.</p>
          <div class="modulo-acciones">
            <a href="${ctx}/proveedores/nuevo" class="btn btn-primary btn-sm"><i class="fa-solid fa-plus me-1"></i>Nuevo proveedor</a>
            <a href="${ctx}/proveedores" class="enlace">Ver proveedores</a>
          </div>
        </article>
      </div>
    </c:if>

    <c:if test="${vm.puedeVerProductos}">
      <div class="col-12 col-sm-6 col-xl-3">
        <article class="modulo">
          <span class="modulo-icono"><i class="fa-solid fa-spray-can-sparkles"></i></span>
          <h2 class="modulo-nombre">Productos</h2>
          <p class="modulo-texto">Perfumes, precios y stock, con aviso cuando algo está por agotarse.</p>
          <div class="modulo-acciones">
            <a href="${ctx}/productos/nuevo" class="btn btn-primary btn-sm"><i class="fa-solid fa-plus me-1"></i>Nuevo producto</a>
            <a href="${ctx}/productos" class="enlace">Ver inventario</a>
          </div>
        </article>
      </div>
    </c:if>

    <c:if test="${vm.puedeVerVentas}">
      <div class="col-12 col-sm-6 col-xl-3">
        <article class="modulo">
          <span class="modulo-icono"><i class="fa-solid fa-cash-register"></i></span>
          <h2 class="modulo-nombre">Ventas</h2>
          <p class="modulo-texto">Registra una venta de varios productos y revisa el historial.</p>
          <div class="modulo-acciones">
            <a href="${ctx}/ventas/nuevo" class="btn btn-primary btn-sm"><i class="fa-solid fa-plus me-1"></i>Nueva venta</a>
            <a href="${ctx}/ventas" class="enlace">Ver ventas</a>
          </div>
        </article>
      </div>
    </c:if>
  </div>
  <p class="pie-yvi">&copy; 2026 Perfumería YVI · Sistema de inventario y ventas</p>
</main>
<%@ include file="/WEB-INF/views/fragmentos/scripts.jspf" %>
</body>
</html>
