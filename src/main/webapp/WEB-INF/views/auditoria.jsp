<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF11 (CU11, HU09) Consultar auditoría y RF12 (CU12) exportar. Solo ADMIN (RF03).
     Enlazada a AuditoriaViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Auditoría - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/fragmentos/barra.jspf" %>
<main class="container-fluid px-lg-5">
  <div class="tarjeta">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-3 gap-2">
      <h1 class="h4 mb-0">Historial de auditoría <span class="badge bg-secondary">${vm.totalRegistros}</span></h1>
      <div class="d-flex gap-2">
        <a href="${ctx}/auditoria/exportar?formato=excel${vm.consultaFiltros}" class="btn btn-success">
          <i class="fa-solid fa-file-excel me-1"></i>Exportar Excel</a>
        <a href="${ctx}/auditoria/exportar?formato=pdf${vm.consultaFiltros}" class="btn btn-danger">
          <i class="fa-solid fa-file-pdf me-1"></i>Exportar PDF</a>
      </div>
    </div>
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>

    <form method="get" action="${ctx}/auditoria" class="row g-3 mb-4">
      <div class="col-md-3">
        <label class="form-label" for="tabla">Tabla</label>
        <select id="tabla" name="tabla" class="form-select">
          <option value="">Todas</option>
          <c:forEach var="t" items="${vm.tablas}">
            <option value="${t}" ${t == vm.filtro.tabla() ? 'selected' : ''}>${t}</option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-3">
        <label class="form-label" for="usuario">Usuario</label>
        <input id="usuario" type="text" name="usuario" class="form-control" placeholder="Nombre de usuario"
               maxlength="100" value="${fn:escapeXml(vm.filtro.usuario())}">
      </div>
      <div class="col-md-3">
        <label class="form-label" for="operacion">Operación</label>
        <select id="operacion" name="operacion" class="form-select">
          <option value="">Todas</option>
          <c:forEach var="o" items="${vm.operaciones}">
            <option value="${o}" ${o == vm.filtro.operacion() ? 'selected' : ''}>${o}</option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-3 d-flex align-items-end gap-2">
        <button type="submit" class="btn btn-primary w-100">Filtrar</button>
        <a href="${ctx}/auditoria" class="btn btn-outline-secondary">Limpiar</a>
      </div>
    </form>

    <div class="row">
      <div class="col-lg-9">
        <div class="table-responsive">
          <table class="table table-striped table-bordered table-sm">
            <thead class="table-dark">
            <tr><th>Fecha</th><th>Tabla</th><th>Operación</th><th>ID registro</th><th>Usuario</th><th>Detalle</th></tr>
            </thead>
            <tbody>
            <c:forEach var="a" items="${vm.registros}">
              <tr>
                <td class="text-nowrap"><c:out value="${a.fecha}"/></td>
                <td><c:out value="${a.tablaAfectada}"/></td>
                <td><c:out value="${a.operacion}"/></td>
                <td>${a.idRegistro}</td>
                <td><c:out value="${a.usuario}"/></td>
                <td class="small"><c:out value="${a.detalle}"/></td>
              </tr>
            </c:forEach>
            <c:if test="${empty vm.registros}">
              <tr><td colspan="6" class="text-center text-muted">No hay registros con esos filtros.</td></tr>
            </c:if>
            </tbody>
          </table>
        </div>
        <nav aria-label="Paginación">
          <ul class="pagination flex-wrap">
            <c:forEach var="i" begin="1" end="${vm.totalPaginas}">
              <li class="page-item ${i == vm.paginaActual ? 'active' : ''}">
                <a class="page-link" href="${ctx}/auditoria?pagina=${i}${vm.consultaFiltros}">${i}</a>
              </li>
            </c:forEach>
          </ul>
        </nav>
      </div>
      <div class="col-lg-3 text-center">
        <h2 class="h6">Resumen por operación</h2>
        <div style="max-width:320px; margin:0 auto;"><canvas id="graficaOperaciones"></canvas></div>
      </div>
    </div>
    <a href="${ctx}/index" class="btn btn-secondary mt-3">Volver al panel</a>
  </div>
</main>
<%@ include file="/WEB-INF/views/fragmentos/scripts.jspf" %>
<script>
  const operaciones = ${vm.operacionesJSON};
  new Chart(document.getElementById('graficaOperaciones'), {
    type: 'pie',
    data: {
      labels: Object.keys(operaciones),
      datasets: [{ data: Object.values(operaciones),
                   backgroundColor: ['#4caf50', '#ff9800', '#f44336', '#2196f3', '#9c27b0', '#607d8b'] }]
    },
    options: { responsive: true }
  });
</script>
</body>
</html>
