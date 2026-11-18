import { useEffect, useState } from 'react';
import { cambiarSimulacion, obtenerSimulacion } from '../api.js';

export default function PanelPruebas() {
  const [abierto, setAbierto] = useState(false);
  const [estado, setEstado] = useState(null);
  const [error, setError] = useState(null);
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    if (!abierto) return;
    obtenerSimulacion().then(setEstado).catch(setError);
  }, [abierto]);

  async function alternar() {
    setGuardando(true);
    setError(null);
    try {
      setEstado(await cambiarSimulacion(!estado?.caida));
    } catch (e) {
      setError(e);
    } finally {
      setGuardando(false);
    }
  }

  return (
    <aside className={`panel-pruebas ${abierto ? 'abierto' : ''}`}>
      <button type="button" className="enlace" onClick={() => setAbierto(!abierto)} aria-expanded={abierto}>
        {abierto ? 'Cerrar panel de pruebas' : 'Panel de pruebas'}
      </button>
      {abierto && (
        <div className="panel-contenido">
          {error && <p className="texto-error">ms-ant no responde: {error.message}</p>}
          {estado && (
            <>
              <p>Modo de la ANT: <strong>{estado.modoAnt}</strong></p>
              <label className="interruptor">
                <input type="checkbox" checked={estado.caida} onChange={alternar} disabled={guardando} />
                Simular caída de la ANT
              </label>
              <p className="texto-ayuda">
                {estado.caida
                  ? 'La ANT se comporta como caída: se responderá con el último dato guardado.'
                  : 'La ANT se consulta normalmente.'}
              </p>
            </>
          )}
        </div>
      )}
    </aside>
  );
}
