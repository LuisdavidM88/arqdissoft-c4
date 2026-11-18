package ec.udla.arqdissoft.sri.cliente;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ec.udla.arqdissoft.sri.error.ServicioExternoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class ClienteSriHttp implements PuertoSri {
    private final RestClient clienteSri;
    private final ObjectMapper mapper;
    private final String urlExiste;
    private final String urlContribuyente;
    private final String urlVehiculo;

    public ClienteSriHttp(RestClient clienteSri, ObjectMapper mapper,
                          @Value("${sri.url-existe-contribuyente}") String urlExiste,
                          @Value("${sri.url-obtener-contribuyente}") String urlContribuyente,
                          @Value("${sri.url-vehiculo}") String urlVehiculo) {
        this.clienteSri = clienteSri;
        this.mapper = mapper;
        this.urlExiste = urlExiste;
        this.urlContribuyente = urlContribuyente;
        this.urlVehiculo = urlVehiculo;
    }

    @Override
    public boolean existeContribuyente(String ruc) {
        String cuerpo = consultar(urlExiste, ruc);
        return cuerpo != null && Boolean.parseBoolean(cuerpo.trim().replace("\"", ""));
    }

    @Override
    public Optional<JsonNode> obtenerContribuyente(String ruc) {
        return primerElemento(consultar(urlContribuyente, ruc));
    }

    @Override
    public Optional<JsonNode> obtenerVehiculo(String placa) {
        return primerElemento(consultar(urlVehiculo, placa));
    }

    private String consultar(String plantillaUrl, String valor) {
        try {
            return clienteSri.get().uri(plantillaUrl, valor).retrieve().body(String.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (RestClientException e) {
            throw new ServicioExternoException(e.getMostSpecificCause().getMessage(), e);
        }
    }

    private Optional<JsonNode> primerElemento(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode nodo = mapper.readTree(cuerpo);
            if (nodo == null || nodo.isNull() || nodo.isMissingNode()) {
                return Optional.empty();
            }
            if (nodo.isArray()) {
                return nodo.isEmpty() ? Optional.empty() : Optional.of(nodo.get(0));
            }
            return nodo.isObject() && nodo.isEmpty() ? Optional.empty() : Optional.of(nodo);
        } catch (JsonProcessingException e) {
            throw new ServicioExternoException("El SRI devolvió una respuesta que no es JSON válido", e);
        }
    }
}
