<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF08 (CU08, HU07) Reporte de ventas; actualizar y eliminar. Enlazada a VentaViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Reportes de ventas - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container">
  <div class="tarjeta">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
      <h1 class="h4 mb-0">Reportes de ventas</h1>
      <a href="${ctx}/ventas/nuevo" class="btn btn-primary"><i class="fa-solid fa-plus me-1"></i>Nueva venta</a>
    </div>
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>
    <div class="row g-3 mb-3">
      <div class="col-sm-6"><div class="border rounded p-3 bg-light">Ventas registradas<div class="fs-4 fw-bold">${fn:length(vm.ventas)}</div></div></div>
      <div class="col-sm-6"><div class="border rounded p-3 bg-light">Total vendido<div class="fs-4 fw-bold">$<fmt:formatNumber value="${vm.totalVendido}" maxFractionDigits="0"/></div></div></div>
    </div>
    <div class="table-responsive">
      <table class="table table-bordered table-striped table-hover">
        <thead class="table-dark">
        <tr><th>ID venta</th><th>Fecha</th><th>Cliente</th><th>Total</th><th>Vendedor</th><th>Acciones</th></tr>
        </thead>
        <tbody>
        <c:forEach var="v" items="${vm.ventas}">
          <tr>
            <td>${v.id}</td>
            <td><c:out value="${v.fecha}"/></td>
            <td><c:out value="${v.cliente}"/></td>
            <td>$<fmt:formatNumber value="${v.total}" maxFractionDigits="0"/></td>
            <td><c:out value="${v.vendedor}"/></td>
            <td class="text-nowrap">
              <a href="${ctx}/ventas/editar?id=${v.id}" class="btn btn-warning btn-sm">Actualizar</a>
              <form method="post" action="${ctx}/ventas/eliminar" class="d-inline"
                    onsubmit="return confirm('¿Eliminar la venta? Las unidades vuelven al inventario.');">
                <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
                <input type="hidden" name="id" value="${v.id}">
                <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty vm.ventas}">
          <tr><td colspan="6" class="text-center text-danger">No hay ventas registradas.</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
    <a href="${ctx}/index" class="btn btn-secondary">Volver</a>
  </div>
</main>
<%@ include file="/WEB-INF/views/fragmentos/scripts.jspf" %>
</body>
</html>
