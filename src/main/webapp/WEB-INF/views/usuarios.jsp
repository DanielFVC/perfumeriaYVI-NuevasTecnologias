<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF09 (CU09, HU08) Gestionar vendedores y RF10 (CU10, HU02) permisos. Solo ADMIN (RF03).
     Enlazada a UsuarioViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Gestión de vendedores - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container">
  <div class="tarjeta">
    <h1 class="h4 mb-3">Gestión de vendedores</h1>
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>

    <h2 class="h6 text-uppercase text-muted">Nuevo vendedor</h2>
    <form action="${ctx}/usuarios/crear" method="post" class="mb-4" autocomplete="off">
      <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
      <div class="row g-3">
        <div class="col-md-4">
          <input type="text" name="full_name" class="form-control" placeholder="Nombre completo" required minlength="3"
                 maxlength="150" value="${fn:escapeXml(vm.nombreNuevo)}" aria-label="Nombre completo">
        </div>
        <div class="col-md-3">
          <input type="text" name="username" class="form-control" placeholder="Usuario" required pattern="[A-Za-z0-9._\-]{4,30}"
                 value="${fn:escapeXml(vm.usernameNuevo)}" aria-label="Usuario">
        </div>
        <div class="col-md-3">
          <input type="password" name="password" class="form-control" placeholder="Contraseña" required minlength="8"
                 maxlength="72" autocomplete="new-password" aria-label="Contraseña">
        </div>
        <div class="col-md-2 d-grid"><button class="btn btn-primary">Crear</button></div>
      </div>
      <div class="form-text">La contraseña debe tener mínimo 8 caracteres, con mayúscula, minúscula y número.
        El vendedor se crea sin permisos; asígnelos en la tabla.</div>
    </form>

    <div class="table-responsive">
      <table class="table table-bordered align-middle">
        <thead class="table-dark">
        <tr><th>ID</th><th>Nombre</th><th>Usuario</th><th>Estado</th><th>Último acceso</th><th>Permisos</th><th>Acciones</th></tr>
        </thead>
        <tbody>
        <c:forEach var="u" items="${vm.vendedores}">
          <tr>
            <td>${u.id}</td>
            <td><c:out value="${u.fullName}"/></td>
            <td><c:out value="${u.username}"/></td>
            <td><span class="badge ${u.active ? 'bg-success' : 'bg-danger'}">${u.active ? 'ACTIVO' : 'BLOQUEADO'}</span></td>
            <td class="small"><c:out value="${empty u.ultimoAcceso ? 'Nunca' : u.ultimoAcceso}"/></td>
            <td>
              <form action="${ctx}/usuarios/permisos" method="post" class="d-flex flex-wrap gap-2 align-items-center">
                <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
                <input type="hidden" name="id" value="${u.id}">
                <label class="form-check-label"><input class="form-check-input me-1" type="checkbox" name="acceso_clientes" value="true" ${u.accesoClientes ? 'checked' : ''}>Clientes</label>
                <label class="form-check-label"><input class="form-check-input me-1" type="checkbox" name="acceso_productos" value="true" ${u.accesoProductos ? 'checked' : ''}>Productos</label>
                <label class="form-check-label"><input class="form-check-input me-1" type="checkbox" name="acceso_proveedores" value="true" ${u.accesoProveedores ? 'checked' : ''}>Proveedores</label>
                <label class="form-check-label"><input class="form-check-input me-1" type="checkbox" name="acceso_ventas" value="true" ${u.accesoVentas ? 'checked' : ''}>Ventas</label>
                <button type="submit" class="btn btn-sm btn-primary">Guardar</button>
              </form>
            </td>
            <td>
              <form action="${ctx}/usuarios/estado" method="post"
                    onsubmit="return confirm('${u.active ? '¿Bloquear a este vendedor?' : '¿Activar a este vendedor?'}');">
                <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
                <input type="hidden" name="id" value="${u.id}">
                <input type="hidden" name="activo" value="${!u.active}">
                <button type="submit" class="btn btn-sm ${u.active ? 'btn-warning' : 'btn-success'}">${u.active ? 'Bloquear' : 'Activar'}</button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty vm.vendedores}">
          <tr><td colspan="7" class="text-center text-muted">No hay vendedores registrados.</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
    <a href="${ctx}/index" class="btn btn-secondary">Volver al panel</a>
  </div>
</main>
<%@ include file="/WEB-INF/views/fragmentos/scripts.jspf" %>
</body>
</html>
