package ec.udla.arqdissoft.sri.error;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
public class ManejadorErrores {
    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> negocio(ApiException e, HttpServletRequest solicitud) {
        return respuesta(e.getStatus(), e.getCodigo(), e.getMessage(), e.getDetalle(), solicitud);
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> sriCaido(ServicioExternoException e, HttpServletRequest solicitud) {
        log.warn("Falla al consultar el SRI: {}", e.getMessage());
        return respuesta(HttpStatus.BAD_GATEWAY, "SRI_NO_DISPONIBLE",
                "El SRI no respondió. Intente nuevamente en unos minutos.", e.getMessage(), solicitud);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException e, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.NOT_FOUND, "RUTA_NO_ENCONTRADA",
                "La ruta solicitada no existe en esta API.", solicitud.getRequestURI(), solicitud);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodo(HttpRequestMethodNotSupportedException e, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO",
                "El método " + e.getMethod() + " no está permitido en esta ruta.", null, solicitud);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception e, HttpServletRequest solicitud) {
        log.error("Error no controlado", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado en el servidor.", null, solicitud);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String codigo, String mensaje,
                                                    String detalle, HttpServletRequest solicitud) {
        ErrorResponse cuerpo = new ErrorResponse(codigo, mensaje, detalle, status.value(),
                solicitud.getRequestURI(), Instant.now().toString());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
