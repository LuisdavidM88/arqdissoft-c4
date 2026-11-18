package ec.udla.arqdissoft.ant.cliente;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "ant", name = "modo", havingValue = "simulado")
public class ClienteAntSimulado implements ClienteAnt {
    @Override
    public ResultadoAnt consultar(String cedula, String placa) {
        int suma = cedula.chars().map(c -> c - '0').sum();
        int citaciones = suma % 3;
        List<List<String>> filas = new ArrayList<>();
        for (int i = 1; i <= citaciones; i++) {
            filas.add(List.of("SIM-" + cedula.substring(6) + "-" + i,
                    placa == null || placa.isBlank() ? "-" : placa,
                    "Contravención de tránsito (dato simulado)",
                    "-3 puntos"));
        }
        return new ResultadoAnt(30 - citaciones * 3, citaciones, filas, ResultadoAnt.ORIGEN_SIMULADO);
    }
}
