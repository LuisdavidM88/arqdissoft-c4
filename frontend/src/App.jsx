import { useState } from 'react';
import { Pasos } from './componentes/Comunes.jsx';
import PanelPruebas from './componentes/PanelPruebas.jsx';
import PasoIdentificacion from './componentes/PasoIdentificacion.jsx';
import PasoLicencia from './componentes/PasoLicencia.jsx';
import PasoVehiculo from './componentes/PasoVehiculo.jsx';
import Resumen from './componentes/Resumen.jsx';

const MODO_DEMO = new URLSearchParams(window.location.search).has('demo');

const DATOS_INICIALES ={ correo: '', ruc: '', contribuyente: null, placa: '', vehiculo: null, puntos: null };

export default function App() {
  const [paso, setPaso] = useState(1);
  const [datos, setDatos] = useState(DATOS_INICIALES);

  const actualizar = (cambios, siguiente) => {
    setDatos((d) => ({ ...d, ...cambios }));
    setPaso(siguiente);
  };

  const nuevaConsulta = () => {
    setDatos(DATOS_INICIALES);
    setPaso(1);
  };

  return (
    <>
      <header className="cabecera">
        <div className="contenedor">
          <h1>Consulta Vehicular</h1>
          <p>Contribuyente y vehículo en el SRI · Puntos de licencia en la ANT</p>
        </div>
      </header>

      <main className="contenedor">
        <Pasos actual={paso} />

        {paso === 1 && (
          <PasoIdentificacion
            inicial={datos}
            onContinuar={(c) => actualizar({ ...c, placa: '', vehiculo: null, puntos: null }, 2)}
          />
        )}
        {paso === 2 && (
          <PasoVehiculo
            inicial={datos}
            onVolver={() => setPaso(1)}
            onContinuar={(c) => actualizar({ ...c, puntos: null }, 3)}
          />
        )}
        {paso === 3 && (
          <PasoLicencia
            cedula={datos.contribuyente.cedula}
            placa={datos.placa}
            inicial={datos.puntos}
            onVolver={() => setPaso(2)}
            onContinuar={(puntos) => actualizar({ puntos }, 4)}
          />
        )}
        {paso === 4 && <Resumen datos={datos} onVolver={() => setPaso(3)} onNuevaConsulta={nuevaConsulta} />}
      </main>

      {MODO_DEMO && <PanelPruebas />}
    </>
  );
}
