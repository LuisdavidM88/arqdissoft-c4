import { useState } from 'react';
import { consultarContribuyente } from '../api.js';
import { normalizarNumero, validarCorreo, validarRuc } from '../validaciones.js';
import { Campo, Cargando, Dato, MensajeError, Vacio } from './Comunes.jsx';

export default function PasoIdentificacion({ inicial, onContinuar }) {
  const [correo, setCorreo] = useState(inicial.correo);
  const [ruc, setRuc] = useState(inicial.ruc);
  const [errores, setErrores] = useState({});
  const [estado, setEstado] = useState(inicial.contribuyente ? 'exito' : 'inicial');
  const [error, setError] = useState(null);
  const [contribuyente, setContribuyente] = useState(inicial.contribuyente);

  async function consultar(evento) {
    evento?.preventDefault();
    const rucLimpio = normalizarNumero(ruc);
    const nuevosErrores = { correo: validarCorreo(correo), ruc: validarRuc(rucLimpio) };
    setErrores(nuevosErrores);
    if (nuevosErrores.correo || nuevosErrores.ruc) return;

    setEstado('cargando');
    setError(null);
    try {
      setContribuyente(await consultarContribuyente(rucLimpio));
      setEstado('exito');
    } catch (e) {
      setError(e);
      setEstado('error');
    }
  }

  function editar() {
    setEstado('inicial');
    setContribuyente(null);
  }

  return (
    <section className="tarjeta" aria-labelledby="titulo-identificacion">
      <h2 id="titulo-identificacion">1. Identificación</h2>
      <p className="intro">Ingresa tu correo y el RUC de la persona natural. Verificaremos en el SRI que sea contribuyente.</p>

      <form onSubmit={consultar} noValidate>
        <div className="fila-campos">
          <Campo id="correo" etiqueta="Correo electrónico" type="email" autoComplete="email"
            placeholder="nombre@correo.com" value={correo} disabled={estado === 'cargando' || estado === 'exito'}
            onChange={(e) => setCorreo(e.target.value)}
            onBlur={() => setErrores((p) => ({ ...p, correo: validarCorreo(correo) }))}
            ayuda="Lo usamos para identificar tu consulta en el resumen." error={errores.correo} />
          <Campo id="ruc" etiqueta="RUC" inputMode="numeric" maxLength={15}
            placeholder="1712345678001" value={ruc} disabled={estado === 'cargando' || estado === 'exito'}
            onChange={(e) => setRuc(e.target.value)}
            onBlur={() => setErrores((p) => ({ ...p, ruc: ruc ? validarRuc(normalizarNumero(ruc)) : null }))}
            ayuda="13 dígitos: tu cédula seguida de 001." error={errores.ruc} />
        </div>
        {estado !== 'exito' && (
          <button type="submit" className="boton" disabled={estado === 'cargando'}>
            Verificar en el SRI
          </button>
        )}
      </form>

      {estado === 'inicial' && (
        <Vacio titulo="Aún no hay datos" texto="Cuando verifiques el RUC verás aquí la información del contribuyente." />
      )}
      {estado === 'cargando' && <Cargando texto="Consultando el catastro del SRI…" />}
      {estado === 'error' && <MensajeError error={error} onReintentar={consultar} />}

      {estado === 'exito' && contribuyente && (
        <div className="resultado" aria-live="polite">
          <div className="resultado-cabecera">
            <span className="insignia insignia-ok">✓ Contribuyente persona natural</span>
            <button type="button" className="enlace" onClick={editar}>Cambiar RUC</button>
          </div>
          <h3>{contribuyente.nombre}</h3>
          <dl className="datos">
            <Dato etiqueta="RUC" valor={contribuyente.ruc} />
            <Dato etiqueta="Cédula" valor={contribuyente.cedula} />
            <Dato etiqueta="Estado" valor={contribuyente.estado} />
            <Dato etiqueta="Régimen" valor={contribuyente.regimen} />
            <Dato etiqueta="Actividad económica" valor={contribuyente.actividadEconomica} />
            <Dato etiqueta="Obligado a llevar contabilidad" valor={contribuyente.obligadoLlevarContabilidad} />
            <Dato etiqueta="Inicio de actividades" valor={contribuyente.fechaInicioActividades} />
          </dl>
          <button type="button" className="boton" onClick={() => onContinuar({ correo: correo.trim(), ruc: normalizarNumero(ruc), contribuyente })}>
            Continuar con el vehículo →
          </button>
        </div>
      )}
    </section>
  );
}
