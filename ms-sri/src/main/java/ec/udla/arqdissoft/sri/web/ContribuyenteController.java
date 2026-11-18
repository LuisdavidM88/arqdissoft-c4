package ec.udla.arqdissoft.sri.web;

import ec.udla.arqdissoft.sri.dominio.Contribuyente;
import ec.udla.arqdissoft.sri.error.ErrorResponse;
import ec.udla.arqdissoft.sri.servicio.ContribuyenteService;
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
@RequestMapping("/api/v1/contribuyentes")
@Tag(name = "Contribuyentes", description = "Verificación en el catastro del SRI")
public class ContribuyenteController {
    private final ContribuyenteService servicio;

    public ContribuyenteController(ContribuyenteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/{ruc}")
    @Operation(summary = "Verifica que el RUC sea de un contribuyente persona natural y devuelve sus datos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contribuyente persona natural"),
            @ApiResponse(responseCode = "400", description = "RUC_INVALIDO",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "NO_ES_CONTRIBUYENTE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "NO_ES_PERSONA_NATURAL",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "SRI_NO_DISPONIBLE",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Contribuyente consultar(@PathVariable String ruc) {
        return servicio.consultar(ruc);
    }
}
