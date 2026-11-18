package ec.udla.arqdissoft.sri.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import ec.udla.arqdissoft.sri.cliente.PuertoSri;
import ec.udla.arqdissoft.sri.dominio.Contribuyente;
import ec.udla.arqdissoft.sri.error.ApiException;
import ec.udla.arqdissoft.sri.validacion.ValidadorIdentificacion;
import ec.udla.arqdissoft.sri.validacion.ValidadorIdentificacion.Resultado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ContribuyenteService {
    static final String PERSONA_NATURAL = "PERSONA NATURAL";

    private final PuertoSri sri;

    public ContribuyenteService(PuertoSri sri) {
        this.sri = sri;
    }

    public Contribuyente consultar(String rucIngresado) {
        String ruc = ValidadorIdentificacion.normalizarNumero(rucIngresado);
        Resultado validacion = ValidadorIdentificacion.validarRuc(ruc);
        if (!validacion.valido()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RUC_INVALIDO",
                    "El RUC ingresado no es válido.", validacion.motivo());
        }

        if (!sri.existeContribuyente(ruc)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NO_ES_CONTRIBUYENTE",
                    "El RUC no está registrado como contribuyente en el SRI.", "RUC consultado: " + ruc);
        }

        JsonNode datos = sri.obtenerContribuyente(ruc).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "CONTRIBUYENTE_SIN_DATOS",
                "El SRI no devolvió información del contribuyente.", "RUC consultado: " + ruc));

        String tipo = texto(datos, "tipoContribuyente");
        if (tipo == null || !PERSONA_NATURAL.equalsIgnoreCase(tipo.trim())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "NO_ES_PERSONA_NATURAL",
                    "El RUC pertenece a un contribuyente que no es persona natural.",
                    "Tipo de contribuyente según el SRI: " + (tipo == null ? "no informado" : tipo));
        }

        return new Contribuyente(
                ruc,
                ruc.substring(0, 10),
                texto(datos, "razonSocial"),
                tipo,
                texto(datos, "estadoContribuyenteRuc"),
                texto(datos, "actividadEconomicaPrincipal"),
                texto(datos, "regimen"),
                texto(datos, "obligadoLlevarContabilidad"),
                soloFecha(texto(datos.path("informacionFechasContribuyente"), "fechaInicioActividades")));
    }

    private static String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo.path(campo);
        return valor.isMissingNode() || valor.isNull() ? null : valor.asText();
    }

    private static String soloFecha(String fechaHora) {
        if (fechaHora == null || fechaHora.isBlank()) {
            return null;
        }
        return fechaHora.length() >= 10 ? fechaHora.substring(0, 10) : fechaHora;
    }
}
