// =====================================================================
// Perfumería YVI - Simulación de un turno de trabajo (para la demo de monitoreo)
// Varios vendedores y el administrador navegan y registran ventas al mismo tiempo,
// así Grafana muestra peticiones, tiempos de respuesta, conexiones a la BD y ventas.
//
//   docker compose --profile monitoreo run --rm trafico
//   (variables opcionales: -e VENDEDORES=8 -e DURACION=10m)
// =====================================================================
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { BASE, tokenCsrf, iniciarSesion, cerrarSesion } from './comun.js';

const VENDEDORES = Number(__ENV.VENDEDORES || 5);
const DURACION = __ENV.DURACION || '5m';

export const options = {
  // Cada vendedor virtual conserva su sesión (cookie) entre vueltas, como un navegador real.
  // Sin esto k6 borra las cookies en cada vuelta y la app lo devuelve al inicio de sesión.
  noCookiesReset: true,
  scenarios: {
    turno: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '30s', target: VENDEDORES },  // llegan los vendedores
        { duration: DURACION, target: VENDEDORES }, // turno normal
        { duration: '30s', target: 0 },            // cierre del turno
      ],
      gracefulRampDown: '10s',
    },
  },
  // RNF-R1: el 95 % de las peticiones debe responder en 2 segundos o menos
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    checks: ['rate>0.95'],
  },
};

// Usuarios de la base ficticia v2 y los módulos que cada uno tiene permitidos (RF03, RF10)
const PERSONAL = [
  { usuario: 'ana.martinez', clave: 'Vendedor2026*', paginas: ['/clientes', '/proveedores', '/productos', '/ventas'], vende: true },
  { usuario: 'carlos.rios', clave: 'Vendedor2026*', paginas: ['/clientes', '/ventas'], vende: true },
  { usuario: 'admin', clave: 'Admin2026*', paginas: ['/productos', '/ventas', '/usuarios', '/auditoria'], vende: false },
];

// Contadores que aparecen en el resumen final de k6
const ventasRegistradas = new Counter('ventas_registradas');
const ventasSinStock = new Counter('ventas_sin_stock');

let token = null; // cada usuario virtual guarda su sesión entre iteraciones

function alAzar(lista) {
  return lista[Math.floor(Math.random() * lista.length)];
}

function vender() {
  const formulario = http.get(`${BASE}/ventas/nuevo`, { tags: { name: 'GET /ventas/nuevo' } });
  // El 70 % de las ventas son de los 8 perfumes más vendidos: su stock baja y se ve en Grafana
  const producto = Math.random() < 0.7 ? 1 + Math.floor(Math.random() * 8) : 1 + Math.floor(Math.random() * 40);
  const datos = [
    `_csrf=${encodeURIComponent(tokenCsrf(formulario))}`,
    `fecha=${new Date().toISOString().slice(0, 10)}`,
    `idCliente=${1 + Math.floor(Math.random() * 40)}`,
    `${encodeURIComponent('idProducto[]')}=${producto}`,
    `${encodeURIComponent('cantidad[]')}=${1 + Math.floor(Math.random() * 2)}`,
  ].join('&');
  const res = http.post(`${BASE}/ventas/registrar`, datos, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    tags: { name: 'POST /ventas/registrar' },
  });
  // Venta registrada: la app redirige al listado de ventas. Si el producto se agotó,
  // responde en el mismo formulario con el aviso "Stock insuficiente" (RF07). Nunca al login.
  check(res, { 'venta procesada': (r) => r.status === 200 && r.url.includes('/ventas') });
  if (res.url.endsWith('/ventas')) {
    ventasRegistradas.add(1);
  } else if (res.body && res.body.includes('Stock insuficiente')) {
    ventasSinStock.add(1);
  }
}

export default function () {
  const persona = PERSONAL[(__VU - 1) % PERSONAL.length];

  if (!token) {
    group('Inicio de sesión', () => {
      token = iniciarSesion(persona.usuario, persona.clave);
    });
    if (!token) {
      sleep(5);
      return;
    }
  }

  group('Consultar módulos', () => {
    for (let i = 0; i < 2; i++) {
      const pagina = alAzar(persona.paginas);
      const res = http.get(`${BASE}${pagina}`, { tags: { name: `GET ${pagina}` } });
      // Debe abrir el módulo, no ser devuelto al inicio de sesión
      check(res, { 'página cargada': (r) => r.status === 200 && !r.url.includes('/login') });
      sleep(1 + Math.random() * 2);
    }
  });

  if (persona.vende && Math.random() < 0.6) {
    group('Registrar venta', vender);
  }

  // De vez en cuando alguien cierra sesión y vuelve a entrar
  if (Math.random() < 0.1) {
    cerrarSesion();
    token = null;
  }
  sleep(2 + Math.random() * 3);
}
