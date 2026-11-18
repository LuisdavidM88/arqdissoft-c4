package ec.udla.arqdissoft.ant.servicio;

import java.util.List;

public record RespuestaPuntos(
        String cedula,
        String placa,
        Integer puntos,
        int totalCitaciones,
        List<List<String>> citaciones,
        String origenDatos,
        String fuente,
        String consultadoEn,
        long edadMinutos,
        boolean esReciente,
        String aviso) {
}
