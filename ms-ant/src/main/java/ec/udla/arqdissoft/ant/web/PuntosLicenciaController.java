package ec.udla.arqdissoft.ant.web;

import ec.udla.arqdissoft.ant.error.ErrorResponse;
import ec.udla.arqdissoft.ant.servicio.PuntosLicenciaService;
import ec.udla.arqdissoft.ant.servicio.RespuestaPuntos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/licencias")
@Tag(name = "Licencias", description = "Puntos de licencia en la ANT con caché en Redis")
public class PuntosLicenciaController {
    private final PuntosLicenciaService servicio;

    public PuntosLicenciaController(PuntosLicenciaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{cedula}/puntos")
    @Operation(summary = "Puntos de la licencia. Si la ANT falla, responde con el último dato bueno y avisa que no es reciente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "fuente = ANT, CACHE o CACHE_RESPALDO"),
            @ApiResponse(responseCode = "400", description = "CEDULA_INVALIDA o PLACA_INVALIDA",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "ANT_NO_DISPONIBLE (sin ANT y sin datos guardados)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RespuestaPuntos consultar(
            @PathVariable String cedula,
            @Parameter(description = "Placa del vehículo (opcional)") @RequestParam(required = false) String placa,
            @Parameter(description = "true para consultar la ANT aunque haya un dato reciente en caché")
            @RequestParam(defaultValue = "false") boolean actualizar) {
        return servicio.consultar(cedula, placa, actualizar);
    }
}
