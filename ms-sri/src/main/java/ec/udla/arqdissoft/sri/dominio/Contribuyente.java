package ec.udla.arqdissoft.sri.dominio;

public record Contribuyente(
        String ruc,
        String cedula,
        String nombre,
        String tipoContribuyente,
        String estado,
        String actividadEconomica,
        String regimen,
        String obligadoLlevarContabilidad,
        String fechaInicioActividades) {
}
