package ec.udla.arqdissoft.ant.error;

public record ErrorResponse(String codigo, String mensaje, String detalle, int status, String ruta, String fecha) {
}
