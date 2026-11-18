package ec.udla.arqdissoft.ant.web;

import ec.udla.arqdissoft.ant.config.AntProperties;
import ec.udla.arqdissoft.ant.servicio.CircuitoAnt;
import ec.udla.arqdissoft.ant.servicio.SimulacionCaida;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/simulacion/ant")
@Tag(name = "Simulación", description = "Prueba de resiliencia: simular la caída de la ANT")
public class SimulacionController {
    public record EstadoSimulacion(boolean caida, String modoAnt) {
    }

    public record CambioSimulacion(boolean caida) {
    }

    private final SimulacionCaida simulacion;
    private final CircuitoAnt circuito;
    private final AntProperties props;

    public SimulacionController(SimulacionCaida simulacion, CircuitoAnt circuito, AntProperties props) {
        this.simulacion = simulacion;
        this.circuito = circuito;
        this.props = props;
    }

    @GetMapping
    @Operation(summary = "Estado de la simulación de caída")
    public EstadoSimulacion estado() {
        return new EstadoSimulacion(simulacion.activa(), props.modo());
    }

    @PutMapping
    @Operation(summary = "Activa o desactiva la caída simulada. Cuerpo: {\"caida\": true}")
    public EstadoSimulacion cambiar(@RequestBody CambioSimulacion cambio) {
        simulacion.cambiar(cambio.caida());
        circuito.reiniciar();
        return estado();
    }
}
