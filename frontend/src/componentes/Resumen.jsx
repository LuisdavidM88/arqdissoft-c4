import { Dato } from './Comunes.jsx';

const FUENTE_TEXTO = { ANT: 'ANT en vivo', CACHE: 'Caché reciente', CACHE_RESPALDO: 'Caché de respaldo (no reciente)' };

export default function Resumen({ datos, onVolver, onNuevaConsulta }) {
  const { correo, contribuyente, vehiculo, puntos } = datos;
  const v = vehiculo?.resumen ?? {};
  return (
    <section className="tarjeta" aria-labelledby="titulo-resumen">
      <h2 id="titulo-resumen">4. Resumen de la consulta</h2>
      <div className="resumen">
        <div>
          <h3>Persona</h3>
          <dl className="datos">
            <Dato etiqueta="Nombre" valor={contribuyente.nombre} />
            <Dato etiqueta="RUC" valor={contribuyente.ruc} />
            <Dato etiqueta="Correo" valor={correo} />
            <Dato etiqueta="Estado en el SRI" valor={contribuyente.estado} />
          </dl>
        </div>
        <div>
          <h3>Vehículo</h3>
          <dl className="datos">
            <Dato etiqueta="Placa" valor={vehiculo.placa} />
            <Dato etiqueta="Marca" valor={v.marca} />
            <Dato etiqueta="Modelo" valor={v.modelo} />
            <Dato etiqueta="Año" valor={v.anio} />
          </dl>
        </div>
        <div>
          <h3>Licencia</h3>
          <dl className="datos">
            <Dato etiqueta="Puntos" valor={puntos.puntos === null ? 'No informado' : `${puntos.puntos} / 30`} />
            <Dato etiqueta="Citaciones" valor={puntos.totalCitaciones} />
            <Dato etiqueta="Origen del dato" valor={FUENTE_TEXTO[puntos.fuente] ?? puntos.fuente} />
          </dl>
          {!puntos.esReciente && <p className="aviso">⚠ El dato de la ANT no es reciente.</p>}
        </div>
      </div>
      <div className="acciones">
        <button type="button" className="boton boton-secundario" onClick={onVolver}>← Volver</button>
        <button type="button" className="boton" onClick={onNuevaConsulta}>Nueva consulta</button>
      </div>
    </section>
  );
}
