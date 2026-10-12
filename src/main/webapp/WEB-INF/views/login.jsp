<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- VIEW (MVVM) - RF01 (CU01, HU01) Iniciar sesión. Enlazada a LoginViewModel (vm). --%>
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Iniciar sesión - PerfumeríaYVI</title>
  <%@ include file="/WEB-INF/views/fragmentos/head.jspf" %>
  <style>
    body { display: flex; align-items: center; justify-content: center; padding: 24px 16px;
           background: radial-gradient(1200px 600px at 50% -10%, #EAE4F1 0%, var(--yvi-porcelana) 60%); }
    .acceso { width: 100%; max-width: 400px; }
    .acceso .marca { font-family: var(--yvi-serif); font-weight: 600; font-size: 2.6rem; line-height: 1;
                     color: var(--yvi-ciruela); text-align: center; margin: 0; }
    .acceso .filete { width: 48px; height: 2px; background: var(--yvi-ambar); margin: 14px auto 10px; border: 0; }
    .acceso .subtitulo { text-align: center; color: var(--yvi-gris); margin-bottom: 28px; }
    .acceso .tarjeta { padding: 28px; }
    .acceso .nota { color: var(--yvi-gris); font-size: .8rem; text-align: center; margin: 18px 0 0; }
  </style>
</head>
<body>
<main class="acceso">
  <h1 class="marca">Perfumería YVI</h1>
  <hr class="filete">
  <p class="subtitulo">Inventario y ventas</p>

  <div class="tarjeta mb-0">
    <%@ include file="/WEB-INF/views/fragmentos/mensajes.jspf" %>
    <form action="${ctx}/login" method="post" autocomplete="off">
      <%@ include file="/WEB-INF/views/fragmentos/csrf.jspf" %>
      <div class="mb-3">
        <label class="form-label" for="username">Usuario</label>
        <input id="username" name="username" type="text" class="form-control" required maxlength="100"
               value="${fn:escapeXml(vm.username)}" autofocus>
      </div>
      <div class="mb-4">
        <label class="form-label" for="password">Contraseña</label>
        <input id="password" name="password" type="password" class="form-control" required maxlength="72"
               autocomplete="current-password">
      </div>
      <div class="d-grid">
        <button class="btn btn-primary" type="submit">Iniciar sesión</button>
      </div>
    </form>
  </div>
  <p class="nota">Por seguridad, la cuenta se bloquea 15 minutos después de 5 intentos fallidos.</p>
</main>
</body>
</html>
