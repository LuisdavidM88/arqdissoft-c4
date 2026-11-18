const URL_MS_SRI = import.meta.env.VITE_URL_MS_SRI ?? 'http://localhost:8081';
const URL_MS_ANT = import.meta.env.VITE_URL_MS_ANT ?? 'http://localhost:8082';

export class ErrorApi extends Error {
  constructor({ codigo, mensaje, detalle, status }) {
    super(mensaje);
    this.codigo = codigo;
    this.detalle = detalle;
    this.status = status;
  }
}

async function pedir(url, opciones = {}) {
  let respuesta;
  try {
    respuesta = await fetch(url, {
      ...opciones,
      headers: { Accept: 'application/json', ...(opciones.body ? { 'Content-Type': 'application/json' } : {}) },
    });
  } catch {
    throw new ErrorApi({
      codigo: 'SIN_CONEXION',
      mensaje: 'No se pudo conectar con el servicio. Verifica que el microservicio esté encendido.',
      detalle: url,
    });
  }

  const texto = await respuesta.text();
  let cuerpo = null;
  try {
    cuerpo = texto ? JSON.parse(texto) : null;
  } catch {
    cuerpo = null;
  }

  if (!respuesta.ok) {
    throw new ErrorApi({
      codigo: cuerpo?.codigo ?? `HTTP_${respuesta.status}`,
      mensaje: cuerpo?.mensaje ?? 'Ocurrió un error inesperado. Intenta nuevamente.',
      detalle: cuerpo?.detalle,
      status: respuesta.status,
    });
  }
  return cuerpo;
}

export const consultarContribuyente = (ruc) =>
  pedir(`${URL_MS_SRI}/api/v1/contribuyentes/${encodeURIComponent(ruc)}`);

export const consultarVehiculo = (placa) =>
  pedir(`${URL_MS_SRI}/api/v1/vehiculos/${encodeURIComponent(placa)}`);

export const consultarPuntos = (cedula, placa, actualizar = false) =>
  pedir(`${URL_MS_ANT}/api/v1/licencias/${encodeURIComponent(cedula)}/puntos`
    + `?placa=${encodeURIComponent(placa ?? '')}&actualizar=${actualizar}`);

export const obtenerSimulacion = () => pedir(`${URL_MS_ANT}/api/v1/simulacion/ant`);

export const cambiarSimulacion = (caida) =>
  pedir(`${URL_MS_ANT}/api/v1/simulacion/ant`, { method: 'PUT', body: JSON.stringify({ caida }) });
