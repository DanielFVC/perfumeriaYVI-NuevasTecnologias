// Funciones compartidas por los scripts de k6 (Perfumería YVI)
import http from 'k6/http';
import { check } from 'k6';
import { parseHTML } from 'k6/html';

// Dentro de Docker la app se llama "app". Para la app de NetBeans use:
//   BASE_URL=http://host.docker.internal:8080/perfumeriaYVI
export const BASE = (__ENV.BASE_URL || 'http://app:8080').replace(/\/$/, '');

/** Token CSRF de la página (RNF-S4): todos los formularios lo llevan en el campo _csrf. */
export function tokenCsrf(res) {
  return parseHTML(res.body).find('input[name="_csrf"]').first().attr('value');
}

/** Inicia sesión (RF01). Devuelve el token CSRF de la sesión nueva o null si falló. */
export function iniciarSesion(usuario, clave) {
  const formulario = http.get(`${BASE}/login`, { tags: { name: 'GET /login' } });
  const res = http.post(`${BASE}/login`,
    { _csrf: tokenCsrf(formulario), username: usuario, password: clave },
    { tags: { name: 'POST /login' } });
  const ok = check(res, {
    'inicio de sesión correcto': (r) => r.status === 200 && !r.url.includes('/login'),
  });
  return ok ? tokenCsrf(res) : null;
}

/**
 * Cierra la sesión (RF02) con el formulario POST de la barra de navegación.
 * Toma el token CSRF de una página recién cargada, igual que hace un navegador real
 * (RNF-S4: un token guardado de antes puede no coincidir y el sistema lo rechazaría).
 */
export function cerrarSesion() {
  const pagina = http.get(`${BASE}/index`, { tags: { name: 'GET /index' } });
  const res = http.post(`${BASE}/logout`, { _csrf: tokenCsrf(pagina) }, { tags: { name: 'POST /logout' } });
  check(res, { 'sesión cerrada': (r) => r.status === 200 && r.url.includes('/login') });
}
