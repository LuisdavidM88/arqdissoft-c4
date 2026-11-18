import { useCallback, useEffect, useRef, useState } from 'react';
import { consultarPuntos } from '../api.js';
import { Cargando, MensajeError, Vacio } from './Comunes.jsx';

const formatoFecha = new Intl.DateTimeFormat('es-EC', {
  dateStyle: 'short', timeStyle: 'short', timeZone: 'America/Guayaquil',
});

const FUENTES = {
  ANT: { clase: 'insignia-ok', texto: 'En vivo desde la ANT' },
  CACHE: { clase: 'insignia-info', texto: 'Desde caché (consultado hace menos de 1 hora)' },
  CACHE_RESPALDO: { clase: 'insignia-alerta', texto: 'Dato de respaldo: la ANT no responde' },
};

function describirEdad(minutos) {
  if (minutos < 1) return 'hace menos de un minuto';
  if (minutos < 60) return `hace ${minutos} min`;
  const horas = Math.floor(minutos / 60);
  if (horas < 24) return `hace ${horas} h`;
  return `hace ${Math.floor(horas / 24)} día(s)`;
}

export default function PasoLicencia({ cedula, placa, inicial, onVolver, onContinuar }) {
  const [estado, setEstado] = useState(inicial ? 'exito' : 'cargando');
  const [error, setError] = useState(null);
  const [puntos, setPuntos] = useState(inicial);

  const consultar = useCallback(async (actualizar = false) => {
    setEstado('cargando');
    setError(null);
    try {
      setPuntos(await consultarPuntos(cedula, placa, actualizar));
      setEstado('exito');
    } catch (e) {
      setError(e);
      setEstado('error');
    }
  }, [cedula, placa]);

  const yaConsultado = useRef(false);
  useEffect(() => {
    if (!inicial && !yaConsultado.current) {
      yaConsultado.current = true;
      consultar();
    }
  }, [inicial, consultar]);

  const fuente = puntos ? FUENTES[puntos.fuente] ?? FUENTES.ANT : null;

  return (
    <section className="tarjeta" aria-labelledby="titulo-licencia">
      <h2 id="titulo-licencia">3. Puntos de licencia</h2>
      <p className="intro">
        Consultamos la ANT con la cédula <strong>{cedula}</strong>. Si la ANT no responde, mostramos el último dato guardado.
      </p>

      {estado === 'cargando' && <Cargando texto="Consultando la ANT (puede tardar hasta 5 segundos)…" />}
      {estado === 'error' && <MensajeError error={error} onReintentar={() => consultar(true)} />}

      {estado === 'exito' && puntos && (
        <div className="resultado" aria-live="polite">
          <div className="resultado-cabecera">
            <span className={`insignia ${fuente.clase}`}>{fuente.texto}</span>
            {puntos.origenDatos === 'SIMULADO' && <span className="insignia insignia-neutra">Datos simulados (modo demo)</span>}
          </div>

          {!puntos.esReciente && puntos.aviso && (
            <div className="aviso" role="status">
              <strong>⚠ Este dato no es reciente.</strong> {puntos.aviso}
            </div>
          )}

          <div className="puntos">
            <span className="puntos-valor">{puntos.puntos ?? '—'}</span>
            <span className="puntos-total">/ 30 puntos</span>
          </div>
          {puntos.puntos === null && (
            <p className="texto-ayuda">La ANT no informó el saldo de puntos en su respuesta.</p>
          )}
          <p className="texto-ayuda">
            Consultado el {formatoFecha.format(new Date(puntos.consultadoEn))} ({describirEdad(puntos.edadMinutos)}).
          </p>

          <h3>Citaciones</h3>
          {puntos.totalCitaciones === 0 ? (
            <Vacio titulo="Sin citaciones registradas" texto="La ANT no reporta citaciones para esta cédula." />
          ) : (
            <div className="tabla-contenedor">
              <table>
                <caption>{puntos.totalCitaciones} registro(s) encontrados</caption>
                <tbody>
                  {puntos.citaciones.map((fila, i) => (
                    <tr key={i}>{fila.map((celda, j) => <td key={j}>{celda}</td>)}</tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="acciones">
            <button type="button" className="boton boton-secundario" onClick={() => consultar(true)}>
              Actualizar desde la ANT
            </button>
            <button type="button" className="boton" onClick={() => onContinuar(puntos)}>Ver resumen →</button>
          </div>
        </div>
      )}

      {estado !== 'exito' && (
        <div className="acciones">
          <button type="button" className="boton boton-secundario" onClick={onVolver}>← Volver</button>
        </div>
      )}
    </section>
  );
}
