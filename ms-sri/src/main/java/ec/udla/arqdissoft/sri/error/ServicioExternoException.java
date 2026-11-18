package ec.udla.arqdissoft.sri.error;

public class ServicioExternoException extends RuntimeException {
    public ServicioExternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
