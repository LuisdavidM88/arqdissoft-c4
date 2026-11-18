package ec.udla.arqdissoft.sri.dominio;

import java.util.Map;

public record Vehiculo(String placa, Map<String, Object> resumen, Map<String, Object> datosSri) {
}
