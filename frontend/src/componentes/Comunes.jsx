export function Pasos({ actual }) {
  const pasos = ['Identificación', 'Vehículo', 'Licencia', 'Resumen'];
  return (
    <ol className="pasos" aria-label="Progreso de la consulta">
      {pasos.map((nombre, i) => {
        const numero = i + 1;
        const estado = numero < actual ? 'hecho' : numero === actual ? 'actual' : 'pendiente';
        return (
          <li key={nombre} className={`paso paso-${estado}`} aria-current={estado === 'actual' ? 'step' : undefined}>
            <span className="paso-numero">{estado === 'hecho' ? '✓' : numero}</span>
            <span className="paso-nombre">{nombre}</span>
          </li>
        );
      })}
    </ol>
  );
}

export function Cargando({ texto }) {
  return (
    <div className="estado estado-cargando" role="status" aria-live="polite">
      <span className="spinner" aria-hidden="true" />
      <span>{texto}</span>
    </div>
  );
}

export function Vacio({ titulo, texto }) {
  return (
    <div className="estado estado-vacio">
      <strong>{titulo}</strong>
      {texto && <p>{texto}</p>}
    </div>
  );
}

export function MensajeError({ error, onReintentar }) {
  if (!error) return null;
  return (
    <div className="estado estado-error" role="alert">
      <div>
        <strong>{error.message}</strong>
        {error.detalle && <p className="detalle">{error.detalle}</p>}
        <p className="codigo">Código: {error.codigo}</p>
      </div>
      {onReintentar && (
        <button type="button" className="boton boton-secundario" onClick={onReintentar}>
          Reintentar
        </button>
      )}
    </div>
  );
}

export function Campo({ id, etiqueta, ayuda, error, ...props }) {
  return (
    <div className="campo">
      <label htmlFor={id}>{etiqueta}</label>
      <input
        id={id}
        aria-invalid={Boolean(error)}
        aria-describedby={`${id}-ayuda`}
        className={error ? 'con-error' : ''}
        {...props}
      />
      <small id={`${id}-ayuda`} className={error ? 'texto-error' : 'texto-ayuda'}>
        {error ?? ayuda}
      </small>
    </div>
  );
}

export function Dato({ etiqueta, valor }) {
  if (valor === null || valor === undefined || valor === '') return null;
  return (
    <div className="dato">
      <dt>{etiqueta}</dt>
      <dd>{String(valor)}</dd>
    </div>
  );
}
