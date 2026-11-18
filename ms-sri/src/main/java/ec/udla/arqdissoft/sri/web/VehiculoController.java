package ec.udla.arqdissoft.sri.web;

import ec.udla.arqdissoft.sri.dominio.Vehiculo;
import ec.udla.arqdissoft.sri.error.ErrorResponse;
import ec.udla.arqdissoft.sri.servicio.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehiculos")
@Tag(name = "Vehículos", description = "Matriculación vehicular del SRI")
public class VehiculoController {
    private final VehiculoService servicio;

    public VehiculoController(VehiculoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{placa}")
    @Operation(summary = "Devuelve la información del vehículo según su placa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @ApiResponse(responseCode = "400", description = "PLACA_INVALIDA",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "VEHICULO_NO_ENCONTRADO",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "SRI_NO_DISPONIBLE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Vehiculo consultar(@PathVariable String placa) {
        return servicio.consultar(placa);
    }
}
