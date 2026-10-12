<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF05 (CU05, HU04) Consultar y eliminar proveedores. Enlazada a ProveedorViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Proveedores - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container-fluid px-lg-5">
  <div class="tarjeta">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
      <h1 class="h4 mb-0">Proveedores registrados <span class="badge bg-secondary">${fn:length(vm.proveedores)}</span></h1>
      <a href="${ctx}/proveedores/nuevo" class="btn btn-primary"><i class="fa-solid fa-plus me-1"></i>Registrar proveedor</a>
    </div>
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>
    <div class="table-responsive">
      <table class="table table-striped table-bordered table-hover">
        <thead class="table-dark">
        <tr>
          <th>ID</th><th>Nombre</th><th>NIT</th><th>Ciudad</th><th>Barrio</th><th>Dirección</th>
          <th>Teléfono</th><th>Email</th><th>Tipo</th><th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="p" items="${vm.proveedores}">
          <tr>
            <td>${p.idProveedor}</td>
            <td><c:out value="${p.nombre}"/></td>
            <td><c:out value="${p.nit}"/></td>
            <td><c:out value="${p.ciudad}"/></td>
            <td><c:out value="${p.barrio}"/></td>
            <td><c:out value="${p.direccion}"/></td>
            <td><c:out value="${p.telefono}"/></td>
            <td><c:out value="${p.email}"/></td>
            <td><c:out value="${p.tipoProveedor}"/></td>
            <td class="text-nowrap">
              <a href="${ctx}/proveedores/editar?id=${p.idProveedor}" class="btn btn-warning btn-sm">Actualizar</a>
              <form method="post" action="${ctx}/proveedores/eliminar" class="d-inline"
                    onsubmit="return confirm('¿Seguro que deseas eliminar este proveedor?');">
                <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
                <input type="hidden" name="id" value="${p.idProveedor}">
                <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty vm.proveedores}">
          <tr><td colspan="10" class="text-center text-danger">No hay proveedores registrados.</td></tr>
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
