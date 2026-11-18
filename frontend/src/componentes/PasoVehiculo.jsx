import { useState } from 'react';
import { consultarVehiculo } from '../api.js';
import { normalizarPlaca, validarPlaca } from '../validaciones.js';
import { Campo, Cargando, Dato, MensajeError, Vacio } from './Comunes.jsx';

const ETIQUETAS = {
  marca: 'Marca', modelo: 'Modelo', anio: 'Año', clase: 'Clase', color: 'Color', cilindraje: 'Cilindraje',
  pais: 'País', servicio: 'Servicio', canton: 'Cantón', ultimoAnioPagado: 'Último año pagado',
  fechaCaducidadMatricula: 'Caducidad de matrícula',
};

const legible = (clave) => clave.replace(/([A-Z])/g, ' $1').replace(/^./, (c) => c.toUpperCase());

export default function PasoVehiculo({ inicial, onVolver, onContinuar }) {
  const [placa, setPlaca] = useState(inicial.placa);
  const [errorCampo, setErrorCampo] = useState(null);
  const [estado, setEstado] = useState(inicial.vehiculo ? 'exito' : 'inicial');
  const [error, setError] = useState(null);
  const [vehiculo, setVehiculo] = useState(inicial.vehiculo);

  async function consultar(evento) {
    evento?.preventDefault();
    const placaLimpia = normalizarPlaca(placa);
    const mensaje = validarPlaca(placaLimpia);
    setErrorCampo(mensaje);
    if (mensaje) return;

    setPlaca(placaLimpia);
    setEstado('cargando');
    setError(null);
    try {
      setVehiculo(await consultarVehiculo(placaLimpia));
      setEstado('exito');
    } catch (e) {
      setError(e);
      setEstado('error');
    }
  }

  const resumen = vehiculo?.resumen ?? {};
  const hayResumen = Object.keys(resumen).length > 0;

  return (
    <section className="tarjeta" aria-labelledby="titulo-vehiculo">
      <h2 id="titulo-vehiculo">2. Vehículo</h2>
      <p className="intro">Ingresa la placa de tu vehículo para obtener su información de matriculación en el SRI.</p>

      <form onSubmit={consultar} noValidate>
        <div className="fila-campos">
          <Campo id="placa" etiqueta="Placa" autoComplete="off" maxLength={9} placeholder="PBA1234"
            value={placa} disabled={estado === 'cargando' || estado === 'exito'}
            onChange={(e) => setPlaca(e.target.value.toUpperCase())}
            ayuda="Sin guion. Auto: ABC1234 · Moto: AB123C." error={errorCampo} />
        </div>
        <div className="acciones">
          <button type="button" className="boton boton-secundario" onClick={onVolver}>← Volver</button>
          {estado !== 'exito' && (
            <button type="submit" className="boton" disabled={estado === 'cargando'}>Buscar vehículo</button>
          )}
        </div>
      </form>

      {estado === 'inicial' && <Vacio titulo="Aún no hay vehículo" texto="Ingresa la placa para ver sus datos." />}
      {estado === 'cargando' && <Cargando texto="Consultando la matriculación vehicular en el SRI…" />}
      {estado === 'error' && <MensajeError error={error} onReintentar={consultar} />}

      {estado === 'exito' && vehiculo && (
        <div className="resultado" aria-live="polite">
          <div className="resultado-cabecera">
            <span className="insignia insignia-ok">✓ Vehículo encontrado</span>
            <button type="button" className="enlace" onClick={() => { setEstado('inicial'); setVehiculo(null); }}>
              Cambiar placa
            </button>
          </div>
          <h3 className="placa">{vehiculo.placa}</h3>
          {hayResumen ? (
            <dl className="datos">
              {Object.entries(resumen).map(([clave, valor]) => (
                <Dato key={clave} etiqueta={ETIQUETAS[clave] ?? legible(clave)} valor={valor} />
              ))}
            </dl>
          ) : (
            <p className="texto-ayuda">El SRI no devolvió los campos principales; revisa el detalle completo.</p>
          )}
          <details className="detalle-completo">
            <summary>Ver todos los datos que devolvió el SRI</summary>
            <dl className="datos">
              {Object.entries(vehiculo.datosSri ?? {}).map(([clave, valor]) => (
                <Dato key={clave} etiqueta={legible(clave)} valor={typeof valor === 'object' ? JSON.stringify(valor) : valor} />
              ))}
            </dl>
          </details>
          <button type="button" className="boton" onClick={() => onContinuar({ placa: vehiculo.placa, vehiculo })}>
            Consultar puntos de licencia →
          </button>
        </div>
      )}
    </section>
  );
}
