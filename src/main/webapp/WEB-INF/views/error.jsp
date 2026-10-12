<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW - Página de error amigable. RNF-F2 (sin errores técnicos) y RF03 (403 acceso denegado). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Aviso - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
</head>
<body class="d-flex align-items-center justify-content-center p-3">
<main class="tarjeta text-center" style="max-width:520px">
  <c:choose>
    <c:when test="${status == 403}">
      <div class="display-6 text-danger mb-2"><i class="fa-solid fa-lock"></i></div>
      <h1 class="h4">Acceso denegado</h1>
      <p class="text-muted"><c:out value="${empty message ? 'No tiene permiso para realizar esta acción.' : message}"/></p>
    </c:when>
    <c:when test="${status == 404}">
      <h1 class="h4">Página no encontrada</h1>
      <p class="text-muted">La dirección que buscó no existe.</p>
    </c:when>
    <c:otherwise>
      <h1 class="h4">Ocurrió un problema</h1>
      <p class="text-muted">No fue posible completar la solicitud. Intente de nuevo en unos minutos.</p>
    </c:otherwise>
  </c:choose>
  <a href="${ctx}/index" class="btn btn-primary">Volver al panel</a>
</main>
</body>
</html>
