// Funciones para hablar con el backend
const CLAVE_TOKEN = 'miPlata_token';

async function api(ruta, { metodo = 'GET', datos = null } = {}) {
  const headers = {};
  const token = localStorage.getItem(CLAVE_TOKEN);

  if (token) headers['Authorization'] = 'Bearer ' + token;
  if (datos) headers['Content-Type'] = 'application/json';

  const respuesta = await fetch('/api' + ruta, {
    method: metodo,
    headers,
    body: datos ? JSON.stringify(datos) : undefined
  });

  const cuerpo = await respuesta.json().catch(() => ({}));

  // Si la sesión venció, vuelve al login
  if (respuesta.status === 401 && !ruta.startsWith('/auth')) {
    localStorage.removeItem(CLAVE_TOKEN);
    window.location.href = 'inicio2.html';
    throw new Error('Sesión vencida');
  }

  if (!respuesta.ok) {
    throw new Error(cuerpo.message || cuerpo.error || 'Error de conexión');
  }

  return cuerpo;
}