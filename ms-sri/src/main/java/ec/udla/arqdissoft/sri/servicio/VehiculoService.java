package ec.udla.arqdissoft.sri.servicio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.arqdissoft.sri.cliente.PuertoSri;
import ec.udla.arqdissoft.sri.dominio.Vehiculo;
import ec.udla.arqdissoft.sri.error.ApiException;
import ec.udla.arqdissoft.sri.validacion.ValidadorIdentificacion;
import ec.udla.arqdissoft.sri.validacion.ValidadorIdentificacion.Resultado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VehiculoService {
    private static final Map<String, List<String>> CAMPOS_RESUMEN = new LinkedHashMap<>();

    static {
        CAMPOS_RESUMEN.put("marca", List.of("descripcionMarca", "marca"));
        CAMPOS_RESUMEN.put("modelo", List.of("descripcionModelo", "modelo"));
        CAMPOS_RESUMEN.put("anio", List.of("anioAuto", "anioModelo", "anio"));
        CAMPOS_RESUMEN.put("clase", List.of("nombreClase", "descripcionClase", "clase"));
        CAMPOS_RESUMEN.put("color", List.of("colorVehiculo1", "color"));
        CAMPOS_RESUMEN.put("cilindraje", List.of("cilindraje"));
        CAMPOS_RESUMEN.put("pais", List.of("descripcionPais", "pais"));
        CAMPOS_RESUMEN.put("servicio", List.of("descripcionServicio", "tipoServicio", "servicio"));
        CAMPOS_RESUMEN.put("canton", List.of("descripcionCanton", "canton"));
        CAMPOS_RESUMEN.put("ultimoAnioPagado", List.of("ultimoAnioPagado"));
        CAMPOS_RESUMEN.put("fechaCaducidadMatricula", List.of("fechaCaducidadMatricula"));
    }

    private final PuertoSri sri;
    private final ObjectMapper mapper;

    public VehiculoService(PuertoSri sri, ObjectMapper mapper) {
        this.sri = sri;
        this.mapper = mapper;
    }

    public Vehiculo consultar(String placaIngresada) {
        String placa = ValidadorIdentificacion.normalizarPlaca(placaIngresada);
        Resultado validacion = ValidadorIdentificacion.validarPlaca(placa);
        if (!validacion.valido()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PLACA_INVALIDA",
                    "La placa ingresada no es válida.", validacion.motivo());
        }

        JsonNode datos = sri.obtenerVehiculo(placa).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "VEHICULO_NO_ENCONTRADO",
                "El SRI no tiene registrado un vehículo con esa placa.", "Placa consultada: " + placa));

        Map<String, Object> resumen = new LinkedHashMap<>();
        CAMPOS_RESUMEN.forEach((nombre, candidatos) -> {
            for (String candidato : candidatos) {
                JsonNode valor = datos.path(candidato);
                if (!valor.isMissingNode() && !valor.isNull() && !valor.asText().isBlank()) {
                    resumen.put(nombre, valor.isNumber() ? valor.numberValue() : valor.asText());
                    return;
                }
            }
        });

        Map<String, Object> datosSri = mapper.convertValue(datos, new TypeReference<LinkedHashMap<String, Object>>() {
        });
        datosSri.values().removeIf(v -> v == null || (v instanceof String s && s.isBlank()));

        return new Vehiculo(placa, resumen, datosSri);
    }
}
