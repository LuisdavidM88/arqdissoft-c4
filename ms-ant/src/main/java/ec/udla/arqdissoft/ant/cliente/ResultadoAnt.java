package ec.udla.arqdissoft.ant.cliente;

import java.util.List;

public record ResultadoAnt(Integer puntos, int totalCitaciones, List<List<String>> citaciones, String origenDatos) {
    public static final String ORIGEN_REAL = "ANT";
    public static final String ORIGEN_SIMULADO = "SIMULADO";
}
