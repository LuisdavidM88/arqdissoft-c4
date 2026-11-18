package ec.udla.arqdissoft.ant.cliente;

import ec.udla.arqdissoft.ant.config.AntProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnProperty(prefix = "ant", name = "modo", havingValue = "real", matchIfMissing = true)
public class ClienteAntHttp implements ClienteAnt {
    private final RestClient clienteAnt;
    private final AnalizadorRespuestaAnt analizador;
    private final AntProperties props;

    public ClienteAntHttp(RestClient clienteAnt, AnalizadorRespuestaAnt analizador, AntProperties props) {
        this.clienteAnt = clienteAnt;
        this.analizador = analizador;
        this.props = props;
    }

    @Override
    public ResultadoAnt consultar(String cedula, String placa) {
        String cuerpo;
        try {
            cuerpo = clienteAnt.get()
                    .uri(props.url(), cedula, placa == null ? "" : placa)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            throw new AntNoDisponibleException("La ANT respondió con error HTTP " + e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new AntNoDisponibleException("No se pudo conectar con la ANT: " + e.getMostSpecificCause().getMessage());
        }
        return analizador.analizar(cuerpo);
    }
}
