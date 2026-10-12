<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF06 (CU06, HU05) Actualizar producto. Enlazada a ProductoViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Actualizar producto - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container">
  <div class="tarjeta">
    <h1 class="h4 mb-3">Actualizar producto</h1>
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>
    <form method="post" action="${ctx}/productos/actualizar" class="row g-3">
      <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
      <input type="hidden" name="id" value="${vm.producto.id}">
      <%@ include file="/WEB-INF/views/fragmentos/form_producto.jspf" %>
      <div class="col-12 mt-4">
        <button type="submit" class="btn btn-success">Guardar cambios</button>
        <a href="${ctx}/productos" class="btn btn-secondary">Cancelar</a>
      </div>
    </form>
  </div>
</main>
<%@ include file="/WEB-INF/views/fragmentos/scripts.jspf" %>
</body>
</html>
