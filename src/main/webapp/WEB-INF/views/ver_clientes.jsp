<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF04 (CU04, HU03) Consultar y eliminar clientes. Enlazada a ClienteViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Clientes - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container-fluid px-lg-5">
  <div class="tarjeta">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
      <h1 class="h4 mb-0">Clientes registrados <span class="badge bg-secondary">${fn:length(vm.clientes)}</span></h1>
      <a href="${ctx}/clientes/nuevo" class="btn btn-primary"><i class="fa-solid fa-plus me-1"></i>Registrar cliente</a>
    </div>
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>
    <div class="table-responsive">
      <table class="table table-striped table-bordered table-hover">
        <thead class="table-dark">
        <tr>
          <th>ID</th><th>Nombre</th><th>Documento</th><th>Teléfono</th><th>Email</th>
          <th>Ciudad</th><th>Barrio</th><th>Dirección</th><th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="c" items="${vm.clientes}">
          <tr>
            <td>${c.idCliente}</td>
            <td><c:out value="${c.nombre}"/></td>
            <td><c:out value="${c.tipoDocumento}"/> <c:out value="${c.numeroDocumento}"/></td>
            <td><c:out value="${c.telefono}"/></td>
            <td><c:out value="${c.email}"/></td>
            <td><c:out value="${c.ciudad}"/></td>
            <td><c:out value="${c.barrio}"/></td>
            <td><c:out value="${c.direccion}"/></td>
            <td class="text-nowrap">
              <a href="${ctx}/clientes/editar?id=${c.idCliente}" class="btn btn-warning btn-sm">Actualizar</a>
              <form method="post" action="${ctx}/clientes/eliminar" class="d-inline"
                    onsubmit="return confirm('¿Seguro que deseas eliminar este cliente?');">
                <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
                <input type="hidden" name="id" value="${c.idCliente}">
                <button type="submit" class="btn btn-danger btn-sm">Eliminar</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty vm.clientes}">
          <tr><td colspan="9" class="text-center text-danger">No hay clientes registrados.</td></tr>
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
