<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF07 (CU07, HU06) Registrar venta con control de stock. Enlazada a VentaViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Nueva venta - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container">
  <div class="tarjeta">
    <h1 class="h4 mb-3">Nueva venta</h1>
    <div class="alert alert-info py-2">Registrando como: <strong><c:out value="${sessionScope.usuarioSesion.nombre}"/></strong></div>
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>
    <form method="post" action="${ctx}/ventas/registrar" class="row g-3">
      <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>

      <%@ include file="/WEB-INF/views/fragmentos/form_venta.jspf" %>
      <div class="col-12 mt-3">
        <button type="submit" class="btn btn-primary">Registrar venta</button>
        <a href="${ctx}/ventas" class="btn btn-secondary">Cancelar</a>
      </div>
    </form>
  </div>
</main>
<%@ include file="/WEB-INF/views/fragmentos/scripts.jspf" %>
<%@ include file="/WEB-INF/views/fragmentos/script_venta.jspf" %>
</body>
</html>
