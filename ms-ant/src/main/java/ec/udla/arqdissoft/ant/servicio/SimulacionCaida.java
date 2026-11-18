package ec.udla.arqdissoft.ant.servicio;

import ec.udla.arqdissoft.ant.config.AntProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SimulacionCaida {
    private final AtomicBoolean activa;

    public SimulacionCaida(AntProperties props) {
        this.activa = new AtomicBoolean(props.simularCaida());
    }

    public boolean activa() {
        return activa.get();
    }

    public void cambiar(boolean valor) {
        activa.set(valor);
    }
}
