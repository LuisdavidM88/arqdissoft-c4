package ec.udla.arqdissoft.ant.servicio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.arqdissoft.ant.cache.AlmacenCache;
import ec.udla.arqdissoft.ant.cache.EntradaCache;
import ec.udla.arqdissoft.ant.cliente.AntNoDisponibleException;
import ec.udla.arqdissoft.ant.cliente.ClienteAnt;
import ec.udla.arqdissoft.ant.cliente.ResultadoAnt;
import ec.udla.arqdissoft.ant.config.AntProperties;
import ec.udla.arqdissoft.ant.error.ApiException;
import ec.udla.arqdissoft.ant.validacion.ValidadorIdentificacion;
import ec.udla.arqdissoft.ant.validacion.ValidadorIdentificacion.Resultado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class PuntosLicenciaService {
    private static final Logger log = LoggerFactory.getLogger(PuntosLicenciaService.class);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Guayaquil"));

    public static final String FUENTE_ANT = "ANT";
    public static final String FUENTE_CACHE = "CACHE";
    public static final String FUENTE_RESPALDO = "CACHE_RESPALDO";

    private final ClienteAnt cliente;
    private final AlmacenCache cache;
    private final ObjectMapper mapper;
    private final Clock reloj;
    private final AntProperties props;
    private final CircuitoAnt circuito;
    private final SimulacionCaida simulacion;

    public PuntosLicenciaService(ClienteAnt cliente, AlmacenCache cache, ObjectMapper mapper, Clock reloj,
                                 AntProperties props, CircuitoAnt circuito, SimulacionCaida simulacion) {
        this.cliente = cliente;
        this.cache = cache;
        this.mapper = mapper;
        this.reloj = reloj;
        this.props = props;
        this.circuito = circuito;
        this.simulacion = simulacion;
    }

    public RespuestaPuntos consultar(String cedulaIngresada, String placaIngresada) {
        return consultar(cedulaIngresada, placaIngresada, false);
    }

    public RespuestaPuntos consultar(String cedulaIngresada, String placaIngresada, boolean forzarActualizacion) {
        String cedula = ValidadorIdentificacion.normalizarNumero(cedulaIngresada);
        Resultado validacion = ValidadorIdentificacion.validarCedula(cedula);
        if (!validacion.valido()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CEDULA_INVALIDA",
                    "La cédula ingresada no es válida.", validacion.motivo());
        }
        String placa = ValidadorIdentificacion.normalizarPlaca(placaIngresada);
        if (!placa.isEmpty()) {
            Resultado validacionPlaca = ValidadorIdentificacion.validarPlaca(placa);
            if (!validacionPlaca.valido()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "PLACA_INVALIDA",
                        "La placa ingresada no es válida.", validacionPlaca.motivo());
            }
        }

        String clave = "ant:puntos:" + cedula + ":" + (placa.isEmpty() ? "-" : placa);
        Instant ahora = reloj.instant();
        Optional<EntradaCache> guardada = leer(clave)
                .filter(e -> edad(e, ahora).compareTo(props.vigencia()) <= 0);

        if (!forzarActualizacion && guardada.isPresent()
                && edad(guardada.get(), ahora).compareTo(props.frescura()) < 0) {
            return respuesta(cedula, placa, guardada.get(), FUENTE_CACHE, ahora, true, null);
        }

        String motivoFalla;
        if (!circuito.permiteLlamar(ahora)) {
            motivoFalla = "La ANT falló varias veces seguidas; se volverá a consultar desde las "
                    + FORMATO.format(circuito.abiertoHasta());
        } else {
            try {
                if (simulacion.activa()) {
                    throw new AntNoDisponibleException("Caída simulada de la ANT (modo de prueba)");
                }
                ResultadoAnt resultado = cliente.consultar(cedula, placa);
                circuito.registrarExito();
                EntradaCache nueva = new EntradaCache(resultado, ahora.toEpochMilli());
                guardar(clave, nueva);
                return respuesta(cedula, placa, nueva, FUENTE_ANT, ahora, true, null);
            } catch (AntNoDisponibleException e) {
                circuito.registrarFallo(ahora);
                motivoFalla = e.getMessage();
                log.warn("ANT no disponible para {}: {}", clave, motivoFalla);
            }
        }

        if (guardada.isPresent()) {
            EntradaCache entrada = guardada.get();
            String aviso = "La ANT no responde en este momento. Se muestra el último dato guardado, del "
                    + FORMATO.format(Instant.ofEpochMilli(entrada.consultadoEnEpochMs()))
                    + "; podría no estar actualizado.";
            return respuesta(cedula, placa, entrada, FUENTE_RESPALDO, ahora, false, aviso);
        }

        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "ANT_NO_DISPONIBLE",
                "La ANT no responde y no hay datos guardados para esta cédula. Intente más tarde.", motivoFalla);
    }

    private RespuestaPuntos respuesta(String cedula, String placa, EntradaCache entrada, String fuente,
                                      Instant ahora, boolean esReciente, String aviso) {
        ResultadoAnt r = entrada.resultado();
        return new RespuestaPuntos(cedula, placa.isEmpty() ? null : placa, r.puntos(), r.totalCitaciones(),
                r.citaciones(), r.origenDatos(), fuente,
                Instant.ofEpochMilli(entrada.consultadoEnEpochMs()).toString(),
                edad(entrada, ahora).toMinutes(), esReciente, aviso);
    }

    private static Duration edad(EntradaCache entrada, Instant ahora) {
        return Duration.between(Instant.ofEpochMilli(entrada.consultadoEnEpochMs()), ahora);
    }

    private Optional<EntradaCache> leer(String clave) {
        try {
            Optional<String> json = cache.leer(clave);
            if (json.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapper.readValue(json.get(), EntradaCache.class));
        } catch (JsonProcessingException | RuntimeException e) {
            log.warn("No se pudo leer la caché ({}): {}", clave, e.getMessage());
            return Optional.empty();
        }
    }

    private void guardar(String clave, EntradaCache entrada) {
        try {
            cache.guardar(clave, mapper.writeValueAsString(entrada), props.vigencia());
        } catch (JsonProcessingException | RuntimeException e) {
            log.warn("No se pudo guardar en la caché ({}): {}", clave, e.getMessage());
        }
    }
}
