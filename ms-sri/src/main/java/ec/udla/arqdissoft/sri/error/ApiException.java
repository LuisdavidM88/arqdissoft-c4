package ec.udla.arqdissoft.sri.error;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;
    private final String detalle;

    public ApiException(HttpStatus status, String codigo, String mensaje, String detalle) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
        this.detalle = detalle;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDetalle() {
        return detalle;
    }
}
