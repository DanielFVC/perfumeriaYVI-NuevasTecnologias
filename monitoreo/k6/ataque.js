// =====================================================================
// Perfumería YVI - Simulación de ataques para la demo de seguridad + monitoreo
// En Grafana y en Alertmanager deben aparecer:
//   1. Cuenta bloqueada tras 5 intentos fallidos      (RNF-S2) -> alerta CuentaBloqueada
//   2. Formulario sin token CSRF rechazado con 403    (RNF-S4) -> alerta PeticionesSinTokenCsrf
//   3. Vendedor que intenta entrar a módulos ajenos   (RF03, RNF-S7)
//
//   docker compose --profile monitoreo run --rm trafico run /scripts/ataque.js
//
// La cuenta sofia.leon queda bloqueada 15 minutos (comportamiento esperado).
// =====================================================================
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { BASE, tokenCsrf, iniciarSesion } from './comun.js';

export const options = { vus: 1, iterations: 1, noCookiesReset: true };

// En este script las respuestas 403 son el resultado esperado (y las redirecciones 3xx del login)
http.setResponseCallback(http.expectedStatuses({ min: 200, max: 399 }, 403));

const VICTIMA = __ENV.VICTIMA || 'sofia.leon';

export default function () {
  group('1. Adivinar la contraseña de ' + VICTIMA + ' (RNF-S2)', () => {
    for (let intento = 1; intento <= 6; intento++) {
      const formulario = http.get(`${BASE}/login`, { tags: { name: 'GET /login' } });
      const res = http.post(`${BASE}/login`,
        { _csrf: tokenCsrf(formulario), username: VICTIMA, password: `Adivinanza${intento}` },
        { tags: { name: 'POST /login (ataque)' } });
      check(res, { 'el sistema no deja entrar': (r) => r.url.includes('/login') });
      sleep(1);
    }
  });

  group('2. Enviar un formulario falso sin token CSRF (RNF-S4)', () => {
    http.get(`${BASE}/login`, { tags: { name: 'GET /login' } });
    const res = http.post(`${BASE}/login`, { username: 'admin', password: 'Admin2026*' },
      { redirects: 0, tags: { name: 'POST /login (sin CSRF)' } });
    check(res, { 'formulario falso rechazado (403)': (r) => r.status === 403 });
  });

  group('3. Vendedor intenta entrar a módulos sin permiso (RF03)', () => {
    if (iniciarSesion('carlos.rios', 'Vendedor2026*')) {
      for (const pagina of ['/usuarios', '/auditoria', '/productos']) {
        const res = http.get(`${BASE}${pagina}`, { tags: { name: `GET ${pagina} (sin permiso)` } });
        check(res, { 'acceso denegado (403)': (r) => r.status === 403 });
        sleep(1);
      }
    }
  });
}
