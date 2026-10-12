<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF06 (CU06, HU05) Inventario con alerta de stock bajo. Enlazada a ProductoViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Inventario - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container-fluid px-lg-5">
  <div class="tarjeta">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
      <h1 class="h4 mb-0">Inventario de productos <span class="badge bg-secondary">${fn:length(vm.productos)}</span></h1>
      <a href="${ctx}/productos/nuevo" class="btn btn-primary"><i class="fa-solid fa-plus me-1"></i>Registrar producto</a>
    </div>
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>
    <c:if test="${vm.cantidadStockBajo > 0}">
      <div class="alert alert-warning">
        <i class="fa-solid fa-triangle-exclamation me-1"></i>
        Hay <strong>${vm.cantidadStockBajo}</strong> producto(s) con stock igual o menor al mínimo (resaltados en amarillo).
      </div>
    </c:if>
    <div class="table-responsive">
      <table class="table table-bordered table-hover">
        <thead class="table-dark">
        <tr>
          <th>ID</th><th>Nombre</th><th>Marca</th><th>Tipo</th><th>Categoría</th><th>Descripción</th>
          <th>Compra</th><th>Venta</th><th>Stock</th><th>Mínimo</th><th>Proveedor</th><th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="p" items="${vm.productos}">
          <tr class="${vm.stockBajo(p) ? 'table-warning' : ''}">
            <td>${p.id}</td>
            <td><c:out value="${p.nombre}"/></td>
            <td><c:out value="${p.marca}"/></td>
            <td><c:out value="${p.tipo}"/></td>
            <td><c:out value="${p.categoria}"/></td>
            <td><c:out value="${p.descripcion}"/></td>
            <td>$<fmt:formatNumber value="${p.precioCompra}" maxFractionDigits="0"/></td>
            <td>$<fmt:formatNumber value="${p.precioVenta}" maxFractionDigits="0"/></td>
            <td class="fw-bold">${p.stock}</td>
            <td>${p.stockMinimo}</td>
            <td><c:out value="${p.nombreProveedor}"/></td>
            <td class="text-nowrap">
              <a href="${ctx}/productos/editar?id=${p.id}" class="btn btn-warning btn-sm">Actualizar</a>
              <form method="post" action="${ctx}/productos/eliminar" class="d-inline"
                    onsubmit="return confirm('¿Seguro que deseas eliminar este producto?');">
                <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
                <input type="hidden" name="id" value="${p.id}">
                <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty vm.productos}">
          <tr><td colspan="12" class="text-center text-danger">No hay productos registrados.</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
    <a href="${ctx}/index" class="btn btn-secondary">Volver al inicio</a>
  </div>
</main>
<%@ include file="/WEB-INF/views/fragmentos/scripts.jspf" %>
</body>
</html>
